using Collector.Communication.DataModel;
using CommonLibrary.Communication.DataModel;
using Encapsulation.Businesslogic;
using Google.Protobuf.WellKnownTypes;
using Moq;
using NLog;
using TestEncapsulation.Helper;

namespace TestEncapsulation.Businesslogic
{
    public class TSEncapsulationBL_Test
    {
        private TestHelper m_TestHelper;

        [SetUp]
        public void Setup()
        {
            m_TestHelper = new TestHelper();
            m_TestHelper.Setup();
        }

        [Test]
        public void Test_Connection()
        {
            Func<TaskRequest, string, Task> functionToCall = null;

            // Setup Message
            TaskRequest message;
            var update = m_TestHelper.CreateLifecycleMessage(out message);
            var eventArgs = new NetworkConversation<Any>();
            eventArgs.Message = Any.Pack(update);
            eventArgs.EndPoint = m_TestHelper.CurrentConnectionInfomation;

            m_TestHelper.MockedCommunicationHelper.Setup(x => x.ServerMessageReceived(null, eventArgs, It.IsAny<Func<TaskRequest, string, Task>>()))
                .Callback<object?, NetworkConversation<Any>, Func<TaskRequest, string, Task>>((s, e, x) => functionToCall = x);

            // Start test
            var instanceUnderTest = new TSEncapsulationBL(m_TestHelper.BasePort, "das", LogManager.GetCurrentClassLogger(), m_TestHelper.MockedCommunicationHelper.Object,
                m_TestHelper.MockedCommunicationFacade.Object);

            // Start asserts
            Assert.NotNull(m_TestHelper.EventHandlerForConversation);
            m_TestHelper.MockedServer.Object.MessageReceived += m_TestHelper.EventHandlerForConversation;

            m_TestHelper.MockedServer.Raise(mock => mock.MessageReceived += null, null, eventArgs);
            Assert.NotNull(functionToCall);

            functionToCall(message, "");

            m_TestHelper.MockedCommunicationHelper.Verify(
                mock => mock.AnnouncingTermination(m_TestHelper.MockedClient.Object, m_TestHelper.MessageForTermination, It.IsAny<string>()), 
                Times.Once
                );
        }
    }
}