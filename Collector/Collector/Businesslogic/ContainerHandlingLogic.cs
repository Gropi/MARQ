using Collector.Communication.DataModel;
using Collector.DockerHandeling;
using Collector.MeasurementExecution;
using Collector.Simulation;
using CommonLibrary.Communication;
using CommonLibrary.Communication.DataModel;
using Google.Protobuf.WellKnownTypes;
using NLog;
using System;
using System.Collections.Concurrent;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Threading.Tasks;

namespace Collector.Businesslogic
{
    internal class ContainerHandlingLogic
    {
        private Logger m_Logger;
        private Logger m_TestRunLogger;
        private ConnectionInformation m_TargetConnectionInformation;
        private ICommunicationFacade m_CommunicationFacade;
        private DockerHandler m_DockerHandler;
        private int m_ManagementPort;
        private ConcurrentDictionary<string, Container> m_ActiveContainers;
        private int m_DummyContainerCount;
        private int m_DummyContainerStartIndex;
        private DateTime m_LastSend = DateTime.Now;

        //TESTING
        private int DUMMYCOUNTER = 1;
        private ConsoleInputReader consoleReader;

        public ContainerHandlingLogic(int managementPort, ConnectionInformation targetConnection, string mode, int dummyContainerCount, Logger applicationLogger, 
            ICommunicationFacade communicationFacade, int dummyContainerStartIndex)
        {
            m_TestRunLogger = LogManager.GetLogger("measurementLogger");
            m_CommunicationFacade = communicationFacade;
            m_Logger = applicationLogger;
            m_Logger.Info("Start new container handler");
            m_ActiveContainers = new ConcurrentDictionary<string, Container>();
            m_DummyContainerCount = dummyContainerCount;
            m_DummyContainerStartIndex = dummyContainerStartIndex;
            DUMMYCOUNTER = m_DummyContainerStartIndex;

            m_TargetConnectionInformation = targetConnection;
            m_ManagementPort= managementPort;

            m_DockerHandler = new DockerHandler(49000);

            if (mode.Equals("blurring"))
            {
                m_Logger.Info("Selected mod is blurring.");
                CreateFaceblurringContainers();
            }
            else
            {
                m_TestRunLogger.Info("Selected mod is dummy.");
                CreateDummyContainers(m_DummyContainerCount);
            }

            consoleReader = new ConsoleInputReader();
            consoleReader.InputRecieved += onInputReceived;

            m_CommunicationFacade.CreateAndInitServerAsync(m_ManagementPort, Server_MessageReceived).Wait();
        }

        private void onInputReceived(object sender, string input)
        {
            Console.WriteLine("Console input recognized: " + input);
            if(input.Equals("reconnect"))
            {
                Console.WriteLine("Begin new registration to server. Amount of registered containers: " + m_ActiveContainers.Keys.Count);
                foreach (var id in m_ActiveContainers.Keys)
                {
                    Container container;
                    m_ActiveContainers.TryGetValue(id, out container);
                    RegisterAtQoRManager(container);
                }
            }
        }

        private async void CreateDummyContainers(int count)
        {
            for (int i = 0; i < count; i++)
            {
                await CreateDummyContainer();
            }
            
        }

        private async void CreateFaceblurringContainers()
        {
            await CreateTSYoloContainer();
            await CreateTSRcnnContainer();
            await CreateFRHogContainer();
            await CreateFRCnnContainer();
            await CreateDeploymentContainer();
            await CreateBlurContainer();
        }

        private async Task CreateTSYoloContainer()
        {
            var id = Guid.NewGuid().ToString();
            //ATTENTION INSERT NAME OF MODEL LIBARY
            var containerPorts = await m_DockerHandler.startObjectRecognitionDocker(id, "predictions/yolov3_d53_coco_pillow", "modelLibary2");
            var container = new Container(id, containerPorts, "yolo");
            m_ActiveContainers.TryAdd(id, container);
            RegisterAtQoRManager(container);
        }

        private async Task CreateTSRcnnContainer()
        {
            var id = Guid.NewGuid().ToString();
            //ATTENTION INSERT NAME OF MODEL LIBARY
            var containerPorts = await m_DockerHandler.startObjectRecognitionDocker(id, "predictions/faster_rcnn_r50_coco_pillow", "modelLibary2");
            var container = new Container(id, containerPorts, "rcnn");
            m_ActiveContainers.TryAdd(id, container);
            RegisterAtQoRManager(container);
        }

        private async Task CreateFRHogContainer()
        {
            var id = Guid.NewGuid().ToString();
            //ATTENTION INSERT NAME OF MODEL LIBARY
            var containerPorts = await m_DockerHandler.startFaceDetectionDocker(id, "faces/hog");
            var container = new Container(id, containerPorts, "hog");
            m_ActiveContainers.TryAdd(id, container);
            RegisterAtQoRManager(container);
        }

        private async Task CreateFRCnnContainer()
        {
            var id = Guid.NewGuid().ToString();
            //ATTENTION INSERT NAME OF MODEL LIBARY
            var containerPorts = await m_DockerHandler.startFaceDetectionDocker(id, "faces/cnn");
            var container = new Container(id, containerPorts, "cnn");
            m_ActiveContainers.TryAdd(id, container);
            RegisterAtQoRManager(container);
        }

        private async Task CreateDeploymentContainer()
        {
            var id = Guid.NewGuid().ToString();
            //ATTENTION INSERT NAME OF MODEL LIBARY
            var containerPorts = await m_DockerHandler.startDeploymentDocker(id);
            var container = new Container(id, containerPorts, "deploy");
            m_ActiveContainers.TryAdd(id, container);
            RegisterAtQoRManager(container);
        }

        private async Task CreateBlurContainer()
        {
            var id = Guid.NewGuid().ToString();
            //ATTENTION INSERT NAME OF MODEL LIBARY
            var containerPorts = await m_DockerHandler.startBlurringDocker(id);
            var container = new Container(id, containerPorts, "blur");
            m_ActiveContainers.TryAdd(id, container);
            RegisterAtQoRManager(container);
        }

        private async Task CreateDummyContainer()
        {
            var id = Guid.NewGuid().ToString();
            //ATTENTION INSERT NAME OF MODEL LIBARY
            var containerPorts = await m_DockerHandler.startDummyDocker(id);
            var container = new Container(id, containerPorts, "dummy" + DUMMYCOUNTER);
            m_ActiveContainers.TryAdd(id, container);
            RegisterAtQoRManager(container);
            DUMMYCOUNTER++;
        }

        private void RegisterAtQoRManager(Container container)
        {
            var registration = new ServiceRegistrationMessage();

            registration.ID = container.UUID;
            registration.ExecutionName = container.Service;
            registration.HandlerPort = m_ManagementPort;

            var keys = container.Ports.Keys;

            for(var i = 0; i < keys.Count; i++)
            {
                var key = keys.ElementAt(i);

                var port = new PurposePort();
                port.Purpose = key;
                port.Port = container.Ports[key];

                registration.PurposePort.Add(port);
            }

            var package = Any.Pack(registration);
            
            var client = m_CommunicationFacade.CreateClient();

            client.SendProtobufToOtherAsync(package, m_TargetConnectionInformation);
        }

        private void Server_MessageReceived(object sender, NetworkConversation<Any> conversation)
        {
            // TODO: Handle errors in the message and send back to client
            var message = conversation.Message;
            if (message != null)
            {
                if (message.Is(SubscriptionLifecycle.Descriptor))
                {
                    var subscriptionLifecycle = message.Unpack<SubscriptionLifecycle>();
                    subscriptionLifecycle.ManagementPort = m_ManagementPort;

                    var client = m_CommunicationFacade.CreateClient();

                    var requestedID = subscriptionLifecycle.TargetID;
                    var requestedContainer = m_ActiveContainers[requestedID];
                    var containerPort = requestedContainer.CollectorPort;

                    if (subscriptionLifecycle.Sub != null)
                    {
                        var sub = subscriptionLifecycle.Sub;
                        var boundries = new Boundries(sub.Type, sub.Information.ExpectedValue, sub.Information.NegativeFailure, sub.Information.PositiveFailure);
                        requestedContainer.ParameterBoundries.Add(sub.Type.ToString(), boundries);
                    }
                    else if (subscriptionLifecycle.Unsubscribe != null)
                    {
                        requestedContainer.ParameterBoundries.Remove(subscriptionLifecycle.Unsubscribe.Type.ToString());
                    }

                    var package = Any.Pack(subscriptionLifecycle);

                    var name = Dns.GetHostName();
                    var ip = Dns.GetHostEntry(name).AddressList.FirstOrDefault(x => x.AddressFamily == AddressFamily.InterNetwork);

                    m_Logger.Debug(m_ManagementPort + " - " + subscriptionLifecycle.ManagementPort);

                    m_Logger.Debug("Sending Request to "+ ip + " " + containerPort);
                    client.SendProtobufToOtherAsync(package, new ConnectionInformation(ip.ToString(), containerPort));
                }
                else if (message.Is(MeasurementEvent.Descriptor))
                {
                    var measurementEvent = message.Unpack<MeasurementEvent>();
                    var container = m_ActiveContainers[measurementEvent.UUID];

                    if(measurementEvent.Cpu != null)
                    {
                        var boundary = container.ParameterBoundries[Types.Cpu.ToString()];
                        if (!checkBoundries((int)measurementEvent.Cpu.CpuUsage, boundary))
                            return;
                        boundary.updateExpectedValue((int)measurementEvent.Cpu.CpuUsage);
                        m_Logger.Info("Sending CPU update at container " + measurementEvent.UUID + "!");
                    }
                    else if(measurementEvent.Network != null)
                    {
                        var boundary = container.ParameterBoundries[Types.Latency.ToString()];
                        if (!checkBoundries((int)measurementEvent.Network.Rtt, boundary))
                            return;
                        boundary.updateExpectedValue((int)measurementEvent.Network.Rtt);
                        m_Logger.Info("Sending RTT update at container " + measurementEvent.UUID + "!");
                    }
                    else if(measurementEvent.Ram != null)
                    {
                        var boundary = container.ParameterBoundries[Types.Ram.ToString()];
                        if (!checkBoundries(measurementEvent.Ram.AvailableMemory, boundary))
                            return;
                        boundary.updateExpectedValue(measurementEvent.Ram.AvailableMemory);
                        m_Logger.Info("Sending RAM update at container " + measurementEvent.UUID + "!");
                    }

                    var elapsedMillisecs = ((TimeSpan)(DateTime.Now - m_LastSend)).TotalMilliseconds;

                    if (elapsedMillisecs > 1000)
                    {
                        var client = m_CommunicationFacade.CreateClient();
                        client.SendProtobufToOtherAsync(message, m_TargetConnectionInformation);
                    }
                }
            }
        }

        private bool checkBoundries(int value, Boundries boundry)
        {
            return value >= boundry.LowerBoundry && value <= boundry.UpperBoundry;
        }
    }
}
