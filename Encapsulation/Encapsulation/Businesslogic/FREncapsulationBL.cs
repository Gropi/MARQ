using Collector.Communication.DataModel;
using CommonLibrary.Communication;
using CommonLibrary.Communication.DataModel;
using Encapsulation.Communication.DataModel;
using Encapsulation.Helper;
using Google.Protobuf.WellKnownTypes;
using NLog;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Net.Http;
using System.Text.Json;
using System.Threading.Tasks;

namespace Encapsulation.Businesslogic
{
    internal class FREncapsulationBL
    {
        private volatile bool m_IsTaskRunning;
        private Logger m_ApplicationLogger;
        private Logger m_TestRunLogger;
        private ICommunicationHelper m_CommunicationHelper;
        private ICommunicationFacade m_CommunicationFacade;
        private Stopwatch m_Watch;
        private string m_Endpoint;

        public int WaitDelay { get; set; } = 1;

        public FREncapsulationBL(int servicePort, string endpoint, Logger applicationLogger, ICommunicationHelper communicationHelper, 
            ICommunicationFacade communicationFacade)
        {
            m_ApplicationLogger = applicationLogger;
            m_TestRunLogger = LogManager.GetLogger("measurementLogger");
            m_CommunicationHelper = communicationHelper;
            m_CommunicationFacade = communicationFacade;

            m_Watch = Stopwatch.StartNew();

            m_Endpoint = endpoint;

            m_IsTaskRunning = false;

            m_CommunicationFacade.CreateAndInitServerAsync(servicePort, ServerMessageReceived).Wait();
        }

        private async Task<TaskRequest> processFacesAsync(TaskRequest task)
        {
            m_ApplicationLogger.Debug("Handling request");
            var message = new TaskRequest();
            var faces = new List<FoundObject>();
            using (var client = new HttpClient())
            {

                var taskContent = task.Content.ToArray();

                m_ApplicationLogger.Info("Recieved " + taskContent.Length + " pictures...");

                m_ApplicationLogger.Debug("------------------");

                for (int i = 0; i < taskContent.Length; i++)
                {

                    m_ApplicationLogger.Info("Handling image " + (i + 1) + "...");

                    var people = JsonSerializer.Deserialize<Person[]>(taskContent[i]);
                    if (people != null)
                    {
                        for (int p = 0; p < people.Length; p++)
                        {
                            var person = people[p];

                            using (var multiPartContent = new MultipartFormDataContent())
                            {
                                //Getting response


                                //m_ApplicationLogger.Info("Postig content at: " + "http://127.0.0.1:8080/" + m_Endpoint + ".");

                                //PREPARE MESSAGE AND ENDPOINT
                                var requestMessage = new HttpRequestMessage(HttpMethod.Post, "http://127.0.0.1:8080/" + m_Endpoint);
                                var bytes = Convert.FromBase64String(person.image);
                                var byteArrayContent = new ByteArrayContent(bytes);

                                //ADD CONTENT
                                multiPartContent.Add(byteArrayContent, "file", "requestImage.jpg");
                                requestMessage.Content = multiPartContent;

                                //SEND AND GET RESPONSE
                                var response = await client.SendAsync(requestMessage);
                                var stringResponse = await response.Content.ReadAsStringAsync();

                                var objects = JsonSerializer.Deserialize<FoundObject[]>(stringResponse);

                                if (objects == null)
                                {
                                    continue;
                                }

                                for (int t = 0; t < objects.Length; t++)
                                {
                                    var face = new FoundObject();

                                    var faceBbox = new decimal[4];
                                    faceBbox[0] = (decimal)objects[t].bbox[0] + person.x0;
                                    faceBbox[1] = (decimal)objects[t].bbox[1] + person.y0;
                                    faceBbox[2] = (decimal)objects[t].bbox[2] + person.x0;
                                    faceBbox[3] = (decimal)objects[t].bbox[3] + person.y0;

                                    face.class_name = objects[t].class_name;
                                    face.bbox = faceBbox;
                                    face.score = 1;

                                    faces.Add(face);
                                    m_ApplicationLogger.Info("| " + (face.bbox[0] + "").PadLeft(4) + " - " + (face.bbox[1] + "").PadLeft(4) + " - " + ((face.bbox[2] - face.bbox[0]) + "").PadLeft(4) + " - " + ((face.bbox[3] - face.bbox[1]) + "").PadLeft(4) + " |");
                                }
                            }
                        }

                        var result = JsonSerializer.Serialize(faces.ToArray());
                        faces = new List<FoundObject>();
                        message.Content.Add(result);
                    } 
                    else
                    {
                        m_ApplicationLogger.Error("FAILED PEOPLE");
                    }
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
                var message = await processFacesAsync(task);

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
            catch(Exception ex)
            {
                m_ApplicationLogger.Error(ex.Message, ex);
                m_IsTaskRunning = false;
            }
        }

        #region Event handling

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
