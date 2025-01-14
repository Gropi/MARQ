using CommonLibrary.Communication.DataModel;
using Google.Protobuf;
using Google.Protobuf.WellKnownTypes;
using NLog;
using System.Net;
using System.Net.Sockets;

namespace CommonLibrary.Communication.Connection.impl
{
    internal class AsynchronousSocketListener : IServer
    {
        #region class variables

        private int m_Port = 2000;
        private Logger m_ApplicationLogger;

        #endregion

        #region Events

        public event EventHandler<NetworkConversation<Any>> MessageReceived;

        #endregion

        private ManualResetEvent allDone = new ManualResetEvent(false);

        public AsynchronousSocketListener(int port, Logger applicationLogger)
        {
            m_Port = port;
            m_ApplicationLogger = applicationLogger;
        }

        public async Task StartServerAsync()
        {
            // Establish the local endpoint for the socket.
            // The DNS name of the computer
            // running the listener is "host.contoso.com".
            var name = Dns.GetHostName(); // get container id
            var ip = Dns.GetHostEntry(name).AddressList.FirstOrDefault(x => x.AddressFamily == AddressFamily.InterNetwork);
            var localEndPoint = new IPEndPoint(ip, m_Port);

            // Create a TCP/IP socket.
            var listener = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);

            // Bind the socket to the local endpoint and listen for incoming connections.
            try
            {
                listener.Bind(localEndPoint);
                listener.Listen(100);

                while (true)
                {
                    // Set the event to nonsignaled state.
                    allDone.Reset();

                    // Start an asynchronous socket to listen for connections.
                    m_ApplicationLogger.Info("Waiting for a connection...");
                    listener.BeginAccept(
                        new AsyncCallback(AcceptCallback),
                        listener);

                    // Wait until a connection is made before continuing.
                    allDone.WaitOne();
                }

            }
            catch (Exception e)
            {
                Console.WriteLine(e.ToString());
                m_ApplicationLogger.Fatal(e.ToString());
            }
        }

        public void AcceptCallback(IAsyncResult ar)
        {
            // Signal the main thread to continue.
            allDone.Set();

            // Get the socket that handles the client request.
            var listener = (Socket)ar.AsyncState;
            var handler = listener.EndAccept(ar);

            var stream = new NetworkStream(handler);
            var protobufStream = new CodedInputStream(stream);
            var remoteEndPoint = handler.RemoteEndPoint as IPEndPoint;
            var address = remoteEndPoint.Address;

            var endPoint = new ConnectionInformation(address.ToString(), remoteEndPoint.Port);
            // TODO: Actually we only parse protobuf messages. 
            var message = Any.Parser.ParseFrom(protobufStream);

            var conversation = new NetworkConversation<Any>(endPoint, message);

            OnMessageReceived(conversation);
        }

        private void OnMessageReceived(NetworkConversation<Any> conversation)
        {
            MessageReceived?.Invoke(this, conversation);
        }
    }
}
