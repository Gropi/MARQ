using Collector.Communication.DataModel;
using CommonLibrary.Communication.Connection;
using CommonLibrary.Communication.DataModel;
using Encapsulation.Communication.DataModel;
using Google.Protobuf.WellKnownTypes;
using NLog;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace Encapsulation.Helper.impl
{
    internal class CommunicationHelper : ICommunicationHelper
    {
        private Logger m_ApplicationLogger;
        public ConnectionInformation ManagementConnectionInformation { get; set; }
        public List<Endpoint> Targets { get; private set; }
        public string ContainerID { get; private set; }
        public IDictionary<string, VectorClock> Clock { get; private set; }

        public CommunicationHelper(Logger applicaationLogger, string containerID)
        {
            m_ApplicationLogger = applicaationLogger;
            Targets = new List<Endpoint>();
            ManagementConnectionInformation = new ConnectionInformation();
            ContainerID = containerID;
            Clock = new Dictionary<string, VectorClock>();
        }

        public TerminationMessage SendToTargets(IClient sender, TaskLifecycle taskLifecycle, TerminationMessage terminationMessage)
        {
            var currentTargets = new List<Endpoint>();
            foreach (var target in Targets)
            {
                if (currentTargets.Where(p => p.IP.Equals(target.IP)).ToList().Count == 0)
                {
                    taskLifecycle = handleClock(taskLifecycle);
                    var package = Any.Pack(taskLifecycle);

                    var successorIP = target.IP;
                    var successorPort = target.Port;
                    var connection = new ConnectionInformation(successorIP, successorPort);

                    m_ApplicationLogger.Info("Sending response to " + successorIP + " " + successorPort);

                    sender.SendProtobufToOtherAsync(package, connection);

                    currentTargets.Add(target);
                }
            }
            return terminationMessage;
        }

        private TaskLifecycle handleClock(string applicationID)
        {
            var clock = getOrAddClock(applicationID);
            clock.Increase();
            var currentLifeCycle = new TaskLifecycle();
            currentLifeCycle.SenderID = clock.ProcessKey;
            currentLifeCycle.AppID = applicationID;
            currentLifeCycle.ProcessStates.Add(clock.ToProcessState());
            return currentLifeCycle;
        }

        private TaskLifecycle handleClock(TaskLifecycle currentLifeCycle)
        {
            var clock = getOrAddClock(currentLifeCycle.AppID);
            clock.Increase();
            currentLifeCycle.SenderID = clock.ProcessKey;
            currentLifeCycle.ProcessStates.Add(clock.ToProcessState());
            return currentLifeCycle;
        }

        public void AnnouncingTermination(IClient connection, TerminationMessage terminationMessage, string applicationID)
        {
            //Announcing termination
            m_ApplicationLogger.Info("Announcing termination...");
            terminationMessage.UUID = ContainerID;

            var finishedAnnouncement = handleClock(applicationID);
            finishedAnnouncement.TerminationMessage = terminationMessage;

            var package = Any.Pack(finishedAnnouncement);

            var managementConnection = new ConnectionInformation(ManagementConnectionInformation.Address, ManagementConnectionInformation.Port);

            connection.SendProtobufToOtherAsync(package, managementConnection);
        }

        public void ClearTargets()
        {
            Targets.Clear();
        }

        public bool ServerMessageReceived(object? sender, NetworkConversation<Any> conversation, Func<TaskRequest, string, Task> handleRequest)
        {
            var message = conversation.Message;
            var handled = false;
            string applicationID = null;
            try
            {
                if (message != null)
                {
                    if (message.Is(TaskLifecycle.Descriptor))
                    {
                        m_ApplicationLogger.Debug("New Tasklifecylce.");
                        var taskLifecycle = message.Unpack<TaskLifecycle>();
                        m_ApplicationLogger.Trace("Lifecycle message: " + taskLifecycle);
                        applicationID = taskLifecycle.AppID;
                        var clock = getOrAddClock(applicationID);

                        if (!clock.IsNewer(taskLifecycle.SenderID, taskLifecycle.ProcessStates.ToList()))
                        {
                            m_ApplicationLogger.Debug("Message is not handeled as corresponding information is outdated. SenderID: " + taskLifecycle.SenderID);
                            return true;
                        }
                        clock.AddSingleProcessState(taskLifecycle.SenderID, taskLifecycle.ProcessStates.ToList());
                        if (taskLifecycle.TaskRequest != null)
                        {
                            m_ApplicationLogger.Debug("Has task request.");
                            m_ApplicationLogger.Debug("Task request message: " + taskLifecycle.TaskRequest);
                            // We add the tagets because we can get request from multiple instances and we need to send the update to all of the 
                            // targets in the end
                            if (taskLifecycle.TaskRequest.Targets != null)
                                Targets.AddRange(taskLifecycle.TaskRequest.Targets.ToList());

                            var port = taskLifecycle.TaskRequest.ManagementPort;
                            if (taskLifecycle.TaskRequest.HasManagementIP)
                                ManagementConnectionInformation = new ConnectionInformation(taskLifecycle.TaskRequest.ManagementIP, port);
                            else
                                ManagementConnectionInformation = new ConnectionInformation(conversation.EndPoint.Address, port);
                            Task.Run(() => handleRequest(taskLifecycle.TaskRequest, taskLifecycle.AppID));
                            handled = true;
                        }
                        else if (taskLifecycle.TargetUpdate != null)
                        {
                            m_ApplicationLogger.Debug("New Target Update.");
                            m_ApplicationLogger.Trace("Target Update message: " + message);
                            Targets = taskLifecycle.TargetUpdate.Targets.ToList();
                            handled = true;
                        }
                    }
                    else if (message.Is(ApplicationEndMessage.Descriptor))
                    {
                        m_ApplicationLogger.Debug("New application end message.");
                        var applicationEndMessage = message.Unpack<ApplicationEndMessage>();
                        m_ApplicationLogger.Trace("Application end message: " + applicationEndMessage);
                        if (Clock.ContainsKey(applicationEndMessage.ApplicationID))
                        {
                            Clock.Remove(applicationEndMessage.ApplicationID);
                        }
                        handled = true;
                    }
                }
                if (handled && applicationID != null)
                {
                    m_ApplicationLogger.Debug("Clock is increased as message was handled.");
                    var clock = getOrAddClock(applicationID);
                    clock.Increase();
                }
            } catch (Exception ex)
            {
                m_ApplicationLogger.Fatal(ex, "Failed to handle sever message.");
            }

            return handled;
        }

        private VectorClock getOrAddClock(string applicationID)
        {
            if (applicationID == null)
            {
                applicationID = "";
                m_ApplicationLogger.Fatal("application id is null.");
            }
            VectorClock applicationClock;
            if (!Clock.ContainsKey(applicationID))
            {
                applicationClock = new VectorClock(m_ApplicationLogger, ContainerID);
                Clock.Add(applicationID, applicationClock);
            } 
            else
            {
                applicationClock = Clock[applicationID];
            }
            return applicationClock;
        }
    }
}
