using Collector.Communication.DataModel;
using CommonLibrary.Communication;
using CommonLibrary.Communication.Connection;
using CommonLibrary.Communication.DataModel;
using Encapsulation.Helper;
using Google.Protobuf.WellKnownTypes;
using Moq;
using System.Threading.Tasks;

namespace TestEncapsulation.Helper
{
    internal class TestHelper
    {
        public Mock<ICommunicationHelper> MockedCommunicationHelper { get; set; } = new Mock<ICommunicationHelper>();
        public Mock<ICommunicationFacade> MockedCommunicationFacade { get; set; } = new Mock<ICommunicationFacade>();
        public ConnectionInformation CurrentConnectionInfomation { get; set; }
        public Mock<IServer> MockedServer { get; set; } = new Mock<IServer>();
        public Mock<IClient> MockedClient { get; set; } = new Mock<IClient>();

        public int BasePort { get; set; } = 2002;
        public string ManagementAddress { get; set; } = "EliasDerFrechdachs";
        public int ManagementPort { get; set; } = 1337;
        public TerminationMessage MessageForTermination { get; set; }
        public EventHandler<NetworkConversation<Any>> EventHandlerForConversation { get; set; }


        public void Setup()
        {
            EventHandlerForConversation = null;

            CurrentConnectionInfomation = new ConnectionInformation(ManagementAddress, ManagementPort);
            MockedCommunicationHelper.Setup(x => x.ManagementConnectionInformation).Returns(() => CurrentConnectionInfomation);

            MockedCommunicationHelper.Setup(x => x.SendToTargets(It.IsAny<IClient>(), It.IsAny<TaskLifecycle>(), It.IsAny<TerminationMessage>()))
                .Callback<IClient, TaskLifecycle, TerminationMessage>((c, a, t) => MessageForTermination = t).Returns(() => MessageForTermination);

            // Setup Mocks
            MockedCommunicationFacade.Setup(x => x.CreateAndInitServerAsync(BasePort, It.IsAny<EventHandler<NetworkConversation<Any>>>()))
                .Callback<int, EventHandler<NetworkConversation<Any>>>((s, x) => EventHandlerForConversation = x).ReturnsAsync(MockedServer.Object);
            MockedCommunicationFacade.Setup(x => x.CreateClient()).Returns(MockedClient.Object);
        }

        public TaskLifecycle CreateLifecycleMessage(out TaskRequest message)
        {
            var targetUpdate = new Endpoint();
            targetUpdate.IP = ManagementAddress;
            targetUpdate.Port = ManagementPort;

            var targets = new List<Endpoint>();
            targets.Add(targetUpdate);
            MockedCommunicationHelper.Setup(x => x.Targets).Returns(targets);

            message = new TaskRequest();
            message.Targets.Add(targets);

            var lifeCycle = new TaskLifecycle();
            lifeCycle.TaskRequest = message;
            lifeCycle.AppID = "dasdawdasd";

            return lifeCycle;
        }

        public TestSetupMessage CreateTestSetup(int mb, params Tuple<Types, int, int>[] parameters)
        {
            var setupMessage = new TestSetupMessage();
            setupMessage.JunkMB = mb;
            foreach (var parameter in parameters)
            {
                var simulated = new TestSetupParameter();
                simulated.ParameterType = parameter.Item1;
                simulated.ExpectedValue = parameter.Item2;
                simulated.PositivePercentage = parameter.Item3;
                setupMessage.SimulatedParameters.Add(simulated);
            }
            return setupMessage;
        }

    }
}
