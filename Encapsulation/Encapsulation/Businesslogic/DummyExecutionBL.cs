using Collector.Communication.DataModel;
using CommonLibrary.Communication;
using CommonLibrary.Communication.DataModel;
using Encapsulation.Helper;
using Encapsulation.Simulation;
using Google.Protobuf.Collections;
using Google.Protobuf.WellKnownTypes;
using NLog;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Threading;
using System.Threading.Tasks;

namespace Encapsulation.Businesslogic
{
    internal class DummyExecutionBL
    {
        private Logger m_TestRunLogger;
        private Logger m_ApplicationLogger;
        private ICommunicationHelper m_CommunicationHelper;
        private ICommunicationFacade m_CommunicationFacade;

        private int m_CallsToWaitFor;

        private int m_ThreadCounter;

        private int m_JunkMB;

        private bool m_IsEndVertex;

        public int WaitDelay { get; set; } = 1;

        private List<SimulatedParameter> m_SimulatedParameters;

        public DummyExecutionBL(int servicePort, Logger applicationLogger, ICommunicationHelper communicationHelper, ICommunicationFacade communicationFacade)
        {
            m_TestRunLogger = LogManager.GetLogger("measurementLogger");
            m_ApplicationLogger = applicationLogger;
            m_CommunicationFacade = communicationFacade;
            m_CommunicationHelper = communicationHelper;

            m_ThreadCounter = 0;
            m_CallsToWaitFor = 0;

            m_JunkMB = 0;

            m_SimulatedParameters = new List<SimulatedParameter>();

            var serverTask = m_CommunicationFacade.CreateAndInitServerAsync(servicePort, ServerMessageReceived);
            serverTask.Wait();
            if (serverTask.Result != null) 
            {
                m_ApplicationLogger.Fatal("No connection establisehd - server is not running.");
            }
            m_ApplicationLogger.Info("Encapsulation: Application stopps.");
        }

        private async Task updateSetupParameters(Stopwatch executionWatch)
        {
            var running = true;
            var delay = 0L;
            while (running)
            {
                foreach(var parameter in m_SimulatedParameters)
                {
                    if (parameter == null)
                    {
                        m_TestRunLogger.Debug("Simulationparameter is null.");
                        continue;
                    }

                    if (parameter.SimulationType.Equals(Types.Time) && delay == 0)
                        delay = parameter.ExpectedValue - executionWatch.ElapsedMilliseconds;
                    /*if (parameter.SimulationType.Equals(Types.Time) && m_Delay == 0)
                        m_Delay = new Random().Next(parameter.LowerBound, parameter.UpperBound + 1);*/
                }
                if (delay <= executionWatch.ElapsedMilliseconds)
                {
                    m_TestRunLogger.Debug("Simulation is done.");
                    running = false;
                } 
                else
                {
                    await Task.Delay(10);
                }
            }
        }

        private void HandleSetupDummy(TestSetupMessage message)
        {
            m_CallsToWaitFor = message.CallsToWaitFor;
            Interlocked.Exchange(ref m_ThreadCounter, m_CallsToWaitFor);
            m_TestRunLogger.Debug("Set threadcounter to " + m_ThreadCounter);
            m_TestRunLogger.Debug("Setup message: " + message.ToString());
            m_JunkMB = message.JunkMB;
            m_IsEndVertex = message.IsEndVertex;
            foreach (var testSetup in message.SimulatedParameters)
            {
                if (testSetup == null)
                    continue;
                m_SimulatedParameters.Add(new SimulatedParameter(testSetup.ParameterType, testSetup.ExpectedValue, testSetup.PositivePercentage));
            }
        }

        private async Task HandleTaskRequestAsync(TaskRequest task, string applicationID)
        {
            m_TestRunLogger.Debug("Recieved TaskRequest!");
            Interlocked.Decrement(ref m_ThreadCounter);
            m_TestRunLogger.Debug("Decreased threadcounter to " + m_ThreadCounter);
            if( m_ThreadCounter > 0)
            {
                m_TestRunLogger.Debug("--- Didn't get in");
                return;
            }
            Interlocked.Exchange(ref m_ThreadCounter, m_CallsToWaitFor);
            m_TestRunLogger.Debug("Set threadcounter to " + m_ThreadCounter);

            var executionWatch = Stopwatch.StartNew();
            var loggingWatch = Stopwatch.StartNew();

            try
            {
                m_ApplicationLogger.Debug("Handling request");
                m_ApplicationLogger.Debug("------------------");

                //Getting response
                var startWaiting = executionWatch.ElapsedMilliseconds;

                var simulationTask = updateSetupParameters(executionWatch);

                var message = new TaskRequest();
                addRandomContent(message.Content, m_JunkMB);

                m_ApplicationLogger.Debug("------------------");

                //Returning response
                message.ManagementPort = m_CommunicationHelper.ManagementConnectionInformation.Port;
                message.ManagementIP = m_CommunicationHelper.ManagementConnectionInformation.Address;

                var lifecycleMessage = new TaskLifecycle();
                lifecycleMessage.TaskRequest = message;
                lifecycleMessage.AppID = applicationID;

                m_ApplicationLogger.Debug("Send message to next client. Message: " + lifecycleMessage);

                simulationTask.Wait();

                executionWatch.Stop();
                var terminationMessage = new TerminationMessage();
                terminationMessage.ExecutionTime = executionWatch.Elapsed.TotalMilliseconds;
                executionWatch.Restart();

                int waitedDelay = 0;

                while (m_CommunicationHelper.Targets.Count == 0 && waitedDelay < 5000 && !m_IsEndVertex)
                {
                    m_ApplicationLogger.Trace("Waiting for successor...");
                    await Task.Delay(WaitDelay);

                    waitedDelay += WaitDelay;
                }

                executionWatch.Stop();
                terminationMessage.IdleTime = executionWatch.Elapsed.TotalMilliseconds;
                executionWatch.Restart();

                var sender = m_CommunicationFacade.CreateClient();
                if (waitedDelay < 5000)
                {
                    terminationMessage = m_CommunicationHelper.SendToTargets(sender, lifecycleMessage, terminationMessage);

                    executionWatch.Stop();
                    terminationMessage.TransmissionTime = executionWatch.Elapsed.TotalMilliseconds;
                    m_CommunicationHelper.AnnouncingTermination(sender, terminationMessage, applicationID);
                    m_CommunicationHelper.ClearTargets();
                } 
                else
                {
                    m_TestRunLogger.Error("Failed to find successor. Object:" + m_CommunicationHelper.Targets);
                    terminationMessage.Error = "Failed to find successor. Not fast enough.";
                    m_CommunicationHelper.AnnouncingTermination(sender, terminationMessage, applicationID);
                    m_CommunicationHelper.ClearTargets();
                }
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.ToString());
                m_ApplicationLogger.Error(ex.Message, ex);
                m_CommunicationHelper.ClearTargets();
            }
        }

        #region Event handling

        private void ServerMessageReceived(object? sender, NetworkConversation<Any> conversation)
        {
            m_ApplicationLogger.Debug("New message received.");
            if (!m_CommunicationHelper.ServerMessageReceived(sender, conversation, HandleTaskRequestAsync))
            {
                var message = conversation.Message;
                if (message != null)
                {
                    if (message.Is(TestSetupMessage.Descriptor))
                    {
                        m_TestRunLogger.Debug("New test setup message arrived.");
                        HandleSetupDummy(message.Unpack<TestSetupMessage>());
                    } 
                    else
                    {
                        m_ApplicationLogger.Error("There was an unexpected communication to server. The conversation is: \r\n" + conversation.Message);
                    }
                }
                // TODO: Handle cases that are not handled by standard receiver...
            }
        }

        #endregion

        private void addRandomContent(RepeatedField<string> content, int sizeInMB)
        {
            for(int i = 0; i < sizeInMB * 1000000; i++)
            {
                content.Add("00000000");
            }
        }
    }
}


