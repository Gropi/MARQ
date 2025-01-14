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
using System.Net.Http;
using System.Text.Json;
using System.Threading.Tasks;
using static System.Net.Mime.MediaTypeNames;

namespace Encapsulation.Businesslogic
{
    internal class TSEncapsulationBL
    {
        private volatile bool m_IsTaskRunning;
        private ICommunicationHelper m_CommunicationHelper;
        private Logger m_ApplicationLogger;
        private Logger m_TestRunLogger;
        private Stopwatch m_Watch;
        private ImageProcesser m_ImageCutter;
        private string m_Endpoint;
        private ICommunicationFacade m_CommunicationFacade;

        public int WaitDelay { get; set; } = 1;

        public TSEncapsulationBL(int servicePort, string endpoint, Logger applicationLogger, ICommunicationHelper communicationHelper, ICommunicationFacade communicationFacade)
        {
            m_TestRunLogger = LogManager.GetLogger("measurementLogger");
            m_ApplicationLogger = applicationLogger;
            m_CommunicationHelper = communicationHelper;
            m_CommunicationFacade = communicationFacade;

            m_Watch = Stopwatch.StartNew();

            m_Endpoint = endpoint;

            m_IsTaskRunning = false;
            m_ImageCutter = new ImageProcesser();

            m_CommunicationFacade.CreateAndInitServerAsync(servicePort, ServerMessageReceived).Wait();
        }

        private async Task<TaskRequest> ObjectDetection(TaskRequest task)
        {
            var message = new TaskRequest();

            m_ApplicationLogger.Info("Handling request");
            m_ApplicationLogger.Info("------------------");
            using (var client = new HttpClient())
            {
                var taskContent = task.Content.ToArray();

                for (int i = 0; i < taskContent.Length; i++)
                {
                    var result = "[";

                    //Getting response
                    var content = new StringContent(taskContent[i]);

                    m_ApplicationLogger.Info("Postig content " + (i + 1) + " at: " + "http://127.0.0.1:8080/" + m_Endpoint + ".");

                    var response = await client.PostAsync("http://127.0.0.1:8080/" + m_Endpoint, content);
                    var stringResponse = await response.Content.ReadAsStringAsync();

                    var objects = JsonSerializer.Deserialize<FoundObject[]>(stringResponse);

                    if (objects == null)
                    {
                        continue;
                    }

                    for (int t = 0; t < objects.Length; t++)
                    {
                        if (!objects[t].class_name.ToLower().Equals("person"))
                        {
                            continue;
                        }

                        var bbox = new int[4];
                        bbox[0] = (int)objects[t].bbox[0];
                        bbox[1] = (int)objects[t].bbox[1];
                        bbox[2] = (int)Math.Ceiling(objects[t].bbox[2]);
                        bbox[3] = (int)Math.Ceiling(objects[t].bbox[3]);

                        var person = new Person();
                        person.x0 = bbox[0];
                        person.y0 = bbox[1];
                        person.image = m_ImageCutter.cutBase64Image(taskContent[i], bbox);

                        if (t != 0)
                        {
                            result += ",";
                        }
                        m_ApplicationLogger.Info("| " + (bbox[0] + "").PadLeft(4) + " - " + (bbox[1] + "").PadLeft(4) + " - " + ((bbox[2] - bbox[0]) + "").PadLeft(4) + " - " + ((bbox[3] - bbox[1]) + "").PadLeft(4) + " |");
                        result += JsonSerializer.Serialize(person);
                    }
                    result += "]";

                    message.Content.Add(result);
                }
            }
            return message;
        }

        private async Task HandleTaskRequestAsync(TaskRequest task, string applicationID)
        {
            if (m_IsTaskRunning)
                return;

            m_Watch.Restart();

            m_IsTaskRunning = true;

            try
            {
                var message = await ObjectDetection(task);

                m_ApplicationLogger.Info("------------------");

                //Returning response
                message.ManagementPort = m_CommunicationHelper.ManagementConnectionInformation.Port;
                message.ManagementIP = m_CommunicationHelper.ManagementConnectionInformation.Address;

                var lifecycleMessage = new TaskLifecycle();
                lifecycleMessage.TaskRequest = message;
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
            catch (Exception ex)
            {
                Console.WriteLine(ex.ToString());
                m_ApplicationLogger.Error(ex.Message, ex);
                m_IsTaskRunning = false;
            }
        }

        #region Event handler

        private void ServerMessageReceived(object? sender, NetworkConversation<Any> conversation)
        {
            if (!m_CommunicationHelper.ServerMessageReceived(sender, conversation, HandleTaskRequestAsync))
            {
                m_ApplicationLogger.Error("There was an unexpected communication to server. The conversation is: \r\n" + conversation.Message);
                // TODO: Handle cases that are not handled by standard receiver...
            }
        }

        #endregion
    }
}

