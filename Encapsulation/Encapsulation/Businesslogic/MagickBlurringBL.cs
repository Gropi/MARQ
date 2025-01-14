using Collector.Communication.DataModel;
using CommonLibrary.Communication;
using CommonLibrary.Communication.DataModel;
using Encapsulation.Communication.DataModel;
using Encapsulation.Helper;
using Google.Protobuf.WellKnownTypes;
using NLog;
using System;
using System.Diagnostics;
using System.Linq;
using System.Text.Json;
using System.Threading.Tasks;
using static System.Net.Mime.MediaTypeNames;

namespace Encapsulation.Businesslogic
{
    internal class MagickBlurringBL
    {
        private volatile bool m_IsTaskRunning;
        private Logger m_ApplicationLogger;
        private Logger m_TestRunLogger;
        private Stopwatch m_Watch;
        private ICommunicationHelper m_CommunicationHelper;
        private ICommunicationFacade m_CommunicationFacade;

        private ImageProcesser m_ImageProcesser;

        public int WaitDelay { get; set; } = 1;

        public MagickBlurringBL(int servicePort, Logger applicationLogger, ICommunicationHelper communicationHelper, ICommunicationFacade communicationFacade)
        {
            m_TestRunLogger = LogManager.GetLogger("measurementLogger");
            m_ApplicationLogger = applicationLogger;
            m_Watch = Stopwatch.StartNew();
            m_CommunicationHelper = communicationHelper;
            m_CommunicationFacade = communicationFacade;

            m_ImageProcesser = new ImageProcesser();
            m_IsTaskRunning = false;

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

            m_Watch.Restart();

            var taskContent = task.Content.ToArray();
            var message = new ReturnMessage();

            for (int i = 0; i < taskContent.Length; i++)
            {
                m_ApplicationLogger.Info("Starting Blur for image " + ((int)(Math.Floor(i/2d) + i%2))   );

                var image = taskContent[i];
                i++;

                var objects = JsonSerializer.Deserialize<FoundObject[]>(taskContent[i]);
                
                if(objects == null || objects.Length == 0)
                {
                    message.Result.Add(image);
                    continue;
                }

                var bboxes = new int[objects.Length][];

                for (int t = 0; t < objects.Length; t++)
                {
                    bboxes[t] = new int[4];
                    bboxes[t][0] =(int)objects[t].bbox[0];
                    bboxes[t][1] = (int)objects[t].bbox[1];
                    bboxes[t][2] = (int)Math.Ceiling(objects[t].bbox[2]);
                    bboxes[t][3] = (int)Math.Ceiling(objects[t].bbox[3]);
                }

                var bluredImage = m_ImageProcesser.BlurAreas(image, bboxes);

                message.Result.Add(bluredImage);
            }

            //Returning response
            var lifecycleMessage = new TaskLifecycle();
            lifecycleMessage.ReturnMessage = message;
            lifecycleMessage.AppID = applicationID;

            var sender = m_CommunicationFacade.CreateClient();

            m_Watch.Stop();
            var terminationMessage = new TerminationMessage();
            terminationMessage.ExecutionTime = m_Watch.Elapsed.TotalMilliseconds;
            m_Watch.Restart();

            while (m_CommunicationHelper.Targets.Count == 0)
            {
                m_ApplicationLogger.Info("Waiting for successor...");
                await Task.Delay(WaitDelay);
            }

            m_Watch.Stop();
            terminationMessage.IdleTime = m_Watch.Elapsed.TotalMilliseconds;
            m_Watch.Restart();

            terminationMessage = m_CommunicationHelper.SendToTargets(sender, lifecycleMessage, terminationMessage);

            m_Watch.Stop();
            terminationMessage.TransmissionTime = m_Watch.Elapsed.TotalMilliseconds;

            m_CommunicationHelper.AnnouncingTermination(sender, terminationMessage, applicationID);
            m_IsTaskRunning = false;
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
