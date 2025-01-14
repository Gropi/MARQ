using CommonLibrary.Communication.Connection;
using CommonLibrary.Communication.Connection.impl;
using CommonLibrary.Communication.DataModel;
using Google.Protobuf.WellKnownTypes;
using NLog;

namespace CommonLibrary.Communication.Facade
{
    public class CommunicationFacade : ICommunicationFacade
    {
        private Logger m_ApplicationLogger;

        public CommunicationFacade(Logger applicationLogger)
        {
            m_ApplicationLogger = applicationLogger;
        }

        public async Task<IServer> CreateAndInitServerAsync(int port, EventHandler<NetworkConversation<Any>> callBack)
        {
            var server = CreateServer(port);
            if (callBack != null)
                server.MessageReceived += callBack;

            await server.StartServerAsync();

            return server;
        }

        public IClient CreateClient()
        {
            return new Client(m_ApplicationLogger);
        }

        public IServer CreateServer(int port)
        {
            return new Server(port, m_ApplicationLogger);
        }
    }
}
