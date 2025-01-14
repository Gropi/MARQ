using CommonLibrary.Communication.DataModel;
using Google.Protobuf;
using NLog;
using System.Net;
using System.Net.Sockets;

namespace CommonLibrary.Communication.Connection.impl
{
    internal class Client : IClient
    {
        private Logger m_Logger;

        public Client(Logger logger)
        {
            m_Logger = logger;
        }

        public async Task<bool> SendProtobufToOtherAsync<T>(T message, ConnectionInformation connectionInformation) where T : IMessage
        {
            try
            {
                return await SendMessageAsync(message.ToByteArray(), connectionInformation);
            }
            catch (Exception e)
            {
                Console.WriteLine(e.ToString());
                m_Logger.Fatal(e);
            }
            return false;
        }

        public async Task<bool> SendMessageAsync(byte[] message, ConnectionInformation connectionInformation)
        {
            try
            {
                m_Logger.Info("Try to establish connection to client with ip: " + connectionInformation.Address
                    + " and port: " + connectionInformation.Port);
                var socket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
                var ipAddress = IPAddress.Parse(connectionInformation.Address);
                var remote = new IPEndPoint(ipAddress, connectionInformation.Port);

                await socket.ConnectAsync(remote);

                socket.Send(message);

                socket.Disconnect(false);
                return true;
            }
            catch (Exception e)
            {
                Console.WriteLine(e.ToString());
                m_Logger.Fatal(e);
            }
            return false;
        }
    }
}
