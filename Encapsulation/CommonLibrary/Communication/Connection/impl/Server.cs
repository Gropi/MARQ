using CommonLibrary.Communication.DataModel;
using Google.Protobuf;
using Google.Protobuf.WellKnownTypes;
using NLog;
using System.Net;
using System.Net.Sockets;

namespace CommonLibrary.Communication.Connection.impl
{
    internal class Server : IServer
    {
        #region class variables

        private int m_Port = 2000;
        private Logger m_ApplicationLogger;

        #endregion

        #region Events

        public event EventHandler<NetworkConversation<Any>> MessageReceived;

        #endregion

        #region Constructor

        public Server(int port, Logger applicationLogger)
        {
            m_Port = port;
            m_ApplicationLogger = applicationLogger;
        }

        #endregion

        public async Task StartServerAsync()
        {
            // Get Host IP Address that is used to establish a connection
            // In this case, we get one IP address of localhost that is IP : 127.0.0.1
            // If a host has multiple addresses, you will get a list of addresses
            var name = Dns.GetHostName(); // get container id
            var ip = Dns.GetHostEntry(name).AddressList.FirstOrDefault(x => x.AddressFamily == AddressFamily.InterNetwork);
            var localEndPoint = new IPEndPoint(IPAddress.Any, m_Port);

            try
            {
                // Create a Socket that will use Tcp protocol
                var listener = new Socket(ip.AddressFamily, SocketType.Stream, ProtocolType.Tcp);
                // A Socket must be associated with an endpoint using the Bind method
                listener.Bind(localEndPoint);
                // Specify how many requests a Socket can listen before it gives Server busy response.
                // We will listen 10 requests at a time
                listener.Listen(100);

                m_ApplicationLogger.Info("Started Server at: " + ip + ":" + m_Port);
                m_ApplicationLogger.Info("Waiting for a connection...");
                while (true)
                {
                    var client = await listener.AcceptAsync();
                    m_ApplicationLogger.Debug("CLIENT CONNECTED");
                    await Task.Run(() => AccecptedClient(client));
                }
            }
            catch (Exception e)
            {
                Console.WriteLine(e.ToString());
                m_ApplicationLogger.Error(e);
            }

            Console.WriteLine("\n Press any key to continue...");
            Console.ReadKey();
        }

        private void AccecptedClient(Socket client)
        {
            try
            {
                while (IsConnected(client))
                {
                    var stream = new NetworkStream(client);
                    var protobufStream = new CodedInputStream(stream);
                    var remoteEndPoint = client.RemoteEndPoint as IPEndPoint;
                    var address = remoteEndPoint.Address;

                    var endPoint = new ConnectionInformation(address.ToString(), remoteEndPoint.Port);
                    // TODO: Actually we only parse protobuf messages. 
                    var message = Any.Parser.ParseFrom(protobufStream);

                    var conversation = new NetworkConversation<Any>(endPoint, message);

                    OnMessageReceived(conversation);
                }
            }
            catch { }
            finally
            {
                if (client.Connected)
                {
                    client.Shutdown(SocketShutdown.Both);
                    client.Close();
                }
            }
        }

        private bool IsConnected(Socket socket)
        {
            try
            {
                return socket != null && !(socket.Poll(1, SelectMode.SelectRead) && socket.Available == 0);
            }
            catch (SocketException) { return false; }
        }

        private void OnMessageReceived(NetworkConversation<Any> conversation)
        {
            MessageReceived?.Invoke(this, conversation);
        }
    }
}
