using Collector.Communication.DataModel;
using Collector.MeasurementExecution;
using Collector.MeasurementExecution.CpuCollection;
using Collector.MeasurementExecution.Dummy;
using Collector.MeasurementExecution.MemoryCollection;
using Collector.MeasurementExecution.PingExecution.impl;
using CommonLibrary.Communication;
using CommonLibrary.Communication.DataModel;
using CommonLibrary.IO;
using CommonLibrary.Parser.impl;
using Google.Protobuf.WellKnownTypes;
using NLog;
using System.Collections.Generic;

namespace Collector.Businesslogic
{
    internal class CollectorBusinesslogic
    {
        private string m_ID;
        private Logger m_ApplicationLogger;
        private Logger m_TestRunLogger;
        private Dictionary<Types, IMeasurmenetUnit> m_Measurements;
        private Dictionary<Types, List<ConnectionInformation>> m_Connections;
        private IOHandler m_IOHandler;
        private int m_ServicePort;
        private List<IMeasurmenetUnit> m_MeasurementUnits;
        private RamMeasurement m_LastSendRAM;
        private CpuMeasurement m_LastSendCPU;
        private NetworkMeasurement m_LastSendLATENCY;
        private ICommunicationFacade m_CommunicationFacade;

        public CollectorBusinesslogic(int servicePort, IOHandler ioHandler, string id, Logger applicationLogger, ICommunicationFacade communicationFacade)
        {
            m_TestRunLogger = LogManager.GetLogger("measurementLogger");
            m_CommunicationFacade = communicationFacade;
            m_ApplicationLogger = applicationLogger;
            m_ApplicationLogger.Info("Starting Collector " + id + " at port: " + servicePort);

            m_ID = id;

            m_Connections = new Dictionary<Types, List<ConnectionInformation>>();

            m_IOHandler = ioHandler;
            m_ServicePort = servicePort;
            m_MeasurementUnits = new List<IMeasurmenetUnit>();

            InitMeasurements();

            m_CommunicationFacade.CreateAndInitServerAsync(m_ServicePort, Server_MessageReceived).Wait();
        }

        private void InitMeasurements()
        {
            var excelHandler = new ExcelParser(m_IOHandler, m_ApplicationLogger);
            m_Measurements = new Dictionary<Types, IMeasurmenetUnit>();

            var latencyMeasurement = new PingExecutor(m_ApplicationLogger);
            latencyMeasurement.OnMeasurmentHappens += OnMeasurmentHappens;
            m_Measurements.Add(Types.Latency, latencyMeasurement);
            m_Connections.Add(Types.Latency, new List<ConnectionInformation>());
            m_MeasurementUnits.Add(latencyMeasurement);

            var memoryMeasurement = new MemoryUnit(m_ApplicationLogger);
            memoryMeasurement.OnMeasurmentHappens += OnMeasurmentHappens;
            m_Measurements.Add(Types.Ram, memoryMeasurement);
            m_Connections.Add(Types.Ram, new List<ConnectionInformation>());
            m_MeasurementUnits.Add(memoryMeasurement);

            var cpuMeasurement = new CpuUnit(m_ApplicationLogger);
            cpuMeasurement.OnMeasurmentHappens += OnMeasurmentHappens;
            m_Measurements.Add(Types.Cpu, cpuMeasurement);
            m_Connections.Add(Types.Cpu, new List<ConnectionInformation>());
            m_MeasurementUnits.Add(cpuMeasurement);
        }

        private void DeinitMeasurements()
        {
            foreach (var unit in m_MeasurementUnits)
            {
                unit.OnMeasurmentHappens -= OnMeasurmentHappens;
            }
            m_MeasurementUnits.Clear();
        }

        private void Server_MessageReceived(object sender, NetworkConversation<Any> conversation)
        {
            // TODO: Handle errors in the message and send back to client
            var message = conversation.Message;
            if (message != null)
            {
                if (message.Is(SubscriptionLifecycle.Descriptor))
                {
                    m_ApplicationLogger.Debug("Handeling subscription lifecycle");
                    var subscriptionLifecycle = message.Unpack<SubscriptionLifecycle>();

                    if (subscriptionLifecycle.Sub != null)
                    {
                        m_Measurements[subscriptionLifecycle.Sub.Type].AddListener(subscriptionLifecycle);
                        AddToConnections(new ConnectionInformation(conversation.EndPoint.Address, subscriptionLifecycle.ManagementPort), subscriptionLifecycle.Sub.Type);
                    }
                    else if (subscriptionLifecycle.Unsubscribe != null)
                    {
                        m_Measurements[subscriptionLifecycle.Unsubscribe.Type].RemoveListener(subscriptionLifecycle);
                        RemoveFromConnections(new ConnectionInformation(conversation.EndPoint.Address, subscriptionLifecycle.ManagementPort), subscriptionLifecycle.Unsubscribe.Type);
                    }
                    else if (subscriptionLifecycle.UpdateSubscription != null)
                    {
                        m_Measurements[subscriptionLifecycle.UpdateSubscription.Type].UpdateListener(subscriptionLifecycle);
                    }
                }
                else if (message.Is(TestSetupMessage.Descriptor))
                {
                    m_ApplicationLogger.Debug("Handeling test setup");
                    DeinitMeasurements();
                    var dummy = new DummyCollection();
                    dummy.OnMeasurmentHappens += OnMeasurmentHappens;
                    m_MeasurementUnits.Add(dummy);
                }
            }
        }

        #region Event handler

        private void OnMeasurmentHappens(object sender, MeasurementEvent measurement)
        {
            m_ApplicationLogger.Trace("Measurement occured");

            measurement.UUID = m_ID;

            var list = new List<ConnectionInformation>();

            if (sender is PingExecutor)
            {
                if (m_LastSendLATENCY == null || m_LastSendLATENCY.Rtt < (measurement.Network.Rtt * 1.1) || measurement.Network.Rtt * 0.9 < m_LastSendLATENCY.Rtt)
                {
                    list = m_Connections[Types.Latency];
                    m_LastSendLATENCY = measurement.Network;
                }
            }
            else if (sender is MemoryUnit)
            {
                if (m_LastSendRAM == null || m_LastSendRAM.AvailableMemory < (measurement.Ram.AvailableMemory * 1.1) || measurement.Ram.AvailableMemory * 0.9 < m_LastSendRAM.AvailableMemory)
                {
                    list = m_Connections[Types.Ram];
                    m_LastSendRAM = measurement.Ram;
                }
            }
            else if (sender is CpuUnit)
            {
                if (m_LastSendCPU == null || m_LastSendCPU.CpuUsage < (measurement.Cpu.CpuUsage * 1.1) || measurement.Cpu.CpuUsage * 0.9 < m_LastSendCPU.CpuUsage)
                {
                    list = m_Connections[Types.Cpu];
                    m_LastSendCPU = measurement.Cpu;
                }
            }
                

            NotifyListeners(list, measurement);
        }

        private void NotifyListeners(List<ConnectionInformation> connections, MeasurementEvent measurement)
        {
            if (connections == null)
                return;

            m_ApplicationLogger.Trace("Notifying " + connections.Count + " listeners");

            var package = Any.Pack(measurement);
            var client = m_CommunicationFacade.CreateClient();
            foreach (var connection in connections)
            {
                client.SendProtobufToOtherAsync(package, connection);
            }
        }

        private void AddToConnections(ConnectionInformation connectionInformation, Types informationType)
        {
            if (!m_Connections.ContainsKey(informationType))
            {
                m_ApplicationLogger.Debug("Added connection");

                var list = new List<ConnectionInformation>();
                list.Add(connectionInformation);
                m_Connections.Add(informationType, list);
            }
            else
            {
                var list = m_Connections[informationType];
                list.Add(connectionInformation);
            }
        }

        private void RemoveFromConnections(ConnectionInformation connectionInformation, Types informationType)
        {
            if (m_Connections.ContainsKey(informationType))
            {
                m_ApplicationLogger.Debug("Removed connection");

                var list = m_Connections[informationType];
                list.Remove(connectionInformation);
            }
        }
        #endregion
    }
}

