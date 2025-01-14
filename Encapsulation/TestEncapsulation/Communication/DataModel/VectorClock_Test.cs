using Collector.Communication.DataModel;
using Encapsulation.Communication.DataModel;
using NLog;

namespace TestEncapsulation.Communication.DataModel
{
    internal class VectorClock_Test
    {
        private VectorClock m_InstanceUnderTest;
        private string m_ProcessState = "TestState";

        [SetUp]
        public void Setup()
        {
            m_InstanceUnderTest = new VectorClock(LogManager.GetCurrentClassLogger(), m_ProcessState);
        }

        [Test]
        public void Test_AddSingleProcessState()
        {
            // Stup
            var processStates = CreateProcessStates(10);
            m_InstanceUnderTest.AddSingleProcessState("5", processStates);

            //Testing
            var clock = m_InstanceUnderTest.Clock;
            foreach (var processState in processStates)
            {
                var result = clock.GetValueOrDefault(processState.Key);

                if (processState != null && processState.Key == "5")
                {
                    Assert.That(result, Is.EqualTo(processState.Value));
                    continue;
                }
                Assert.That(result, Is.EqualTo(0));
            }
            Assert.That(clock.GetValueOrDefault(m_ProcessState), Is.EqualTo(0));
        }

        [Test]
        public void Test_AddProcessStates()
        {
            // Stup
            var processStates = CreateProcessStates(10);
            m_InstanceUnderTest.AddProcessStates(processStates);

            //Testing
            var clock = m_InstanceUnderTest.Clock;
            foreach (var processState in processStates)
            {
                var result = clock.GetValueOrDefault(processState.Key);
                Assert.That(result, Is.EqualTo(processState.Value));
            }
            Assert.That(clock.GetValueOrDefault(m_ProcessState), Is.EqualTo(0));
        }

        [Test]
        public void Test_IsNewer_KeyList_Newer()
        {
            // Stup
            var processStates = CreateProcessStates(10);
            m_InstanceUnderTest.AddProcessStates(processStates);

            //Testing
            var processState = processStates[7];
            processState.Value = 100;

            Assert.True(m_InstanceUnderTest.IsNewer(processState.Key, processStates));
        }

        [Test]
        public void Test_IsNewer_KeyList_Older()
        {
            // Stup
            var processStates = CreateProcessStates(10);
            m_InstanceUnderTest.AddProcessStates(processStates);

            //Testing
            var processState = processStates[7];
            processState.Value = 3;

            Assert.False(m_InstanceUnderTest.IsNewer(processState.Key, processStates));
        }

        private List<ProcessState> CreateProcessStates(int amount)
        {
            var processStates = new List<ProcessState>(); 
            for (int i = 0; i < amount; i++) 
            { 
                var state = new ProcessState();
                state.Key = i.ToString();
                state.Value = i;
                processStates.Add(state);
            }
            return processStates;
        }
    }
}
