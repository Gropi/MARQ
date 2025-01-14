using Collector.Communication.DataModel;
using CommonLibrary.Communication.DataModel;
using Encapsulation.Businesslogic;
using Google.Protobuf.WellKnownTypes;
using Moq;
using NLog;
using System.Diagnostics;
using TestEncapsulation.Helper;

namespace TestEncapsulation.Businesslogic
{
    internal class DummyExecutionBL_Test
    {
        private TestHelper m_TestHelper;

        [SetUp]
        public void Setup()
        {
            m_TestHelper = new TestHelper();
            m_TestHelper.Setup();
        }

        [Test]
        [TestCase(3000)]
        [TestCase(2000)]
        [TestCase(1000)]
        [TestCase(500)]
        public void HandleSetup_Ok(int expectedTime)
        {
            Func<TaskRequest, string, Task> functionToCall = null;

            var setupMessage = m_TestHelper.CreateTestSetup(10, new Tuple<Types, int, int>(Types.Time, expectedTime, 10));
            var simulationMessage = new NetworkConversation<Any>();
            simulationMessage.Message = Any.Pack(setupMessage);
            simulationMessage.EndPoint = m_TestHelper.CurrentConnectionInfomation;

            // Setup Message
            TaskRequest message;
            var update = m_TestHelper.CreateLifecycleMessage(out message);
            var startRequest = new NetworkConversation<Any>();
            startRequest.Message = Any.Pack(update);
            startRequest.EndPoint = m_TestHelper.CurrentConnectionInfomation;

            m_TestHelper.MockedCommunicationHelper.Setup(x => x.ServerMessageReceived(null, startRequest, It.IsAny<Func<TaskRequest, string, Task>>()))
                .Callback<object?, NetworkConversation<Any>, Func<TaskRequest, string, Task>>((s, e, x) => functionToCall = x);

            var instanceUnderTest = new DummyExecutionBL(m_TestHelper.BasePort, LogManager.GetCurrentClassLogger(), m_TestHelper.MockedCommunicationHelper.Object,
                m_TestHelper.MockedCommunicationFacade.Object);

            // Start asserts
            Assert.NotNull(m_TestHelper.EventHandlerForConversation);
            m_TestHelper.MockedServer.Object.MessageReceived += m_TestHelper.EventHandlerForConversation;

            m_TestHelper.MockedServer.Raise(mock => mock.MessageReceived += null, null, simulationMessage);

            // Start test
            var timer = Stopwatch.StartNew();

            m_TestHelper.MockedServer.Raise(mock => mock.MessageReceived += null, null, startRequest);
            Assert.NotNull(functionToCall);
            functionToCall(message, "");

            var startWaiting = timer.ElapsedMilliseconds;

            // check if the time is correctly used
            Assert.IsTrue(startWaiting >= expectedTime * 0.9);

            m_TestHelper.MockedCommunicationHelper.Verify(
                mock => mock.AnnouncingTermination(m_TestHelper.MockedClient.Object, m_TestHelper.MessageForTermination, It.IsAny<string>()),
                Times.Once
                );
        }
    }
}
