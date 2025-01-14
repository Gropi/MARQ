namespace CommonLibrary.Communication.DataModel
{
    public class NetworkConversation<T>
    {
        public ConnectionInformation EndPoint { get; set; }

        public T Message { get; set; }

        public NetworkConversation() { }

        public NetworkConversation(ConnectionInformation endPoint, T message)
        {
            EndPoint = endPoint;
            Message = message;
        }
    }
}
