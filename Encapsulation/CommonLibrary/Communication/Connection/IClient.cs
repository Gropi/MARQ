using CommonLibrary.Communication.DataModel;
using Google.Protobuf;

namespace CommonLibrary.Communication.Connection
{
    public interface IClient
    {
        Task<bool> SendProtobufToOtherAsync<T>(T message, ConnectionInformation connectionInformation) where T : IMessage;

        Task<bool> SendMessageAsync(byte[] message, ConnectionInformation connectionInformation);
    }
}
