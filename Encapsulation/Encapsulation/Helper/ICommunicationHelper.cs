using Collector.Communication.DataModel;
using CommonLibrary.Communication.Connection;
using CommonLibrary.Communication.DataModel;
using Google.Protobuf.WellKnownTypes;
using System;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace Encapsulation.Helper
{
    public interface ICommunicationHelper
    {
        #region Properties

        ConnectionInformation ManagementConnectionInformation { get; set; }
        public List<Endpoint> Targets { get; }
        public string ContainerID { get; }

        #endregion

        #region Methods

        TerminationMessage SendToTargets(IClient sender, TaskLifecycle taskLifecycle, TerminationMessage terminationMessage);
        void AnnouncingTermination(IClient connection, TerminationMessage terminationMessage, string applicaitonID);
        void ClearTargets();
        bool ServerMessageReceived(object? sender, NetworkConversation<Any> conversation, Func<TaskRequest, string, Task> handleRequest);

        #endregion
    }
}
