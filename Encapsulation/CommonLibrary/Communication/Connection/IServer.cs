using CommonLibrary.Communication.DataModel;
using Google.Protobuf.WellKnownTypes;

namespace CommonLibrary.Communication.Connection
{
    public interface IServer
    {
        event EventHandler<NetworkConversation<Any>> MessageReceived;

        Task StartServerAsync();
    }
}
