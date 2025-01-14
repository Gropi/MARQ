using CommonLibrary.Communication.Connection;
using CommonLibrary.Communication.DataModel;
using Google.Protobuf.WellKnownTypes;

namespace CommonLibrary.Communication
{
    public interface ICommunicationFacade
    {
        IServer CreateServer(int port);

        Task<IServer> CreateAndInitServerAsync(int port, EventHandler<NetworkConversation<Any>> callBack);

        IClient CreateClient();
    }
}
