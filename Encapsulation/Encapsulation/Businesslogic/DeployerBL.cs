using Collector.Communication.DataModel;
using CommonLibrary.Communication;
using CommonLibrary.Communication.DataModel;
using Encapsulation.Helper;
using Google.Protobuf.WellKnownTypes;
using NLog;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Threading.Tasks;

namespace Encapsulation.Businesslogic
{
    internal class DeployerBL
    {
        private volatile bool m_IsTaskRunning;
        private Logger m_ApplicationLogger;
        private Logger m_TestRunLogger;
        private Stopwatch m_Watch;
        private ICommunicationFacade m_CommunicationFacade;
        private ICommunicationHelper m_CommunicationHelper;

        public int WaitDelay { get; set; } = 1;

        private List<string[]> m_BaseImages;

        public DeployerBL(int servicePort, Logger applicationLogger, ICommunicationHelper communicationHelper, ICommunicationFacade communicationFacade)
        {
            m_ApplicationLogger = applicationLogger;
            m_TestRunLogger = LogManager.GetLogger("measurementLogger");
            m_CommunicationHelper = communicationHelper;
            m_CommunicationFacade = communicationFacade;

            m_Watch = Stopwatch.StartNew();
            m_BaseImages = new List<string[]>();

            m_CommunicationFacade.CreateAndInitServerAsync(servicePort, ServerMessageReceived).Wait();
        }

        private async Task HandleTaskRequestAsync(TaskRequest task, string applicationID)
        {
            if (m_IsTaskRunning)
            {
                m_ApplicationLogger.Info(String.Format("There is a request while the application is still running. From: {0}", task.ManagementIP));
                return;
            }

            m_ApplicationLogger.Debug("Handling request");

            var taskContent = task.Content.ToArray();

            if (taskContent[0].ToLower().Equals("upload_base_images"))
            {
                m_ApplicationLogger.Debug("Got images");
                var contentArray = new string[taskContent.Length - 1];
                for (int i = 0; i < contentArray.Length; i++)
                {
                    contentArray[i] = taskContent[i + 1];
                }

                m_BaseImages.Add(contentArray);
            }
            else
            {
                m_IsTaskRunning = true;

                var terminationMessage = new TerminationMessage();

                m_Watch.Restart();

                while (m_BaseImages.Count == 0)
                {
                    m_ApplicationLogger.Info("Waiting for successor...");
                    await Task.Delay(WaitDelay);
                }

                m_Watch.Stop();
                terminationMessage.IdleTime = m_Watch.Elapsed.TotalMilliseconds;
                m_Watch.Restart();

                //Returning response
                var imageList = m_BaseImages.ElementAt(0);
                m_BaseImages.RemoveAt(0);

                var message = new TaskRequest();

                //Fill message
                for (int i = 0; i < imageList.Length; i++)
                {
                    try
                    {
                        message.Content.Add(imageList[i]);
                        message.Content.Add(taskContent[i]);
                    }
                    catch (IndexOutOfRangeException e)
                    {
                        Console.WriteLine(e.ToString());
                        m_ApplicationLogger.Error("Couldn't resolve all task images only processing " + i + " images!");
                        break;
                    }

                }

                message.ManagementPort = m_CommunicationHelper.ManagementConnectionInformation.Port;
                message.ManagementIP = m_CommunicationHelper.ManagementConnectionInformation.Address;

                var lifecycleMessage = new TaskLifecycle();
                lifecycleMessage.TaskRequest = message;
                lifecycleMessage.AppID = applicationID;

                var sender = m_CommunicationFacade.CreateClient();

                m_Watch.Stop();
                terminationMessage.ExecutionTime = m_Watch.Elapsed.TotalMilliseconds;
                m_Watch.Restart();

                while (m_CommunicationHelper.Targets.Count == 0)
                {
                    await Task.Delay(WaitDelay);
                }

                m_Watch.Stop();
                terminationMessage.IdleTime = m_Watch.Elapsed.TotalMilliseconds;
                m_Watch.Restart();

                terminationMessage = m_CommunicationHelper.SendToTargets(sender, lifecycleMessage, terminationMessage);

                m_ApplicationLogger.Info("Response content:");
                foreach(var c in message.Content) {
                    m_ApplicationLogger.Debug(c.ToString());
                }

                m_Watch.Stop();
                terminationMessage.TransmissionTime = m_Watch.Elapsed.TotalMilliseconds;

                m_CommunicationHelper.AnnouncingTermination(sender, terminationMessage, applicationID);
                m_IsTaskRunning = false;
            }
        }

        private void ServerMessageReceived(object? sender, NetworkConversation<Any> conversation)
        {
            if (!m_CommunicationHelper.ServerMessageReceived(sender, conversation, HandleTaskRequestAsync))
            {
                m_ApplicationLogger.Error("There was an unexpected communication to server. The conversation is: \r\n" + conversation.Message);
                // TODO: Handle cases that are not handled by standard receiver...
            }
        }
    }
}
