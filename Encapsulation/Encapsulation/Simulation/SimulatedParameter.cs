using Collector.Communication.DataModel;

namespace Encapsulation.Simulation
{
    public class SimulatedParameter
    {
        public Types SimulationType { get; private set; }
        public int ExpectedValue { get; private set; }
        public int LowerBound { get; private set; }
        public int UpperBound { get; private set; }

        public SimulatedParameter(Types type, int expectedValue, int range)
        {
            SimulationType = type;
            ExpectedValue = expectedValue;
            LowerBound = expectedValue - ((expectedValue / 100) * range);
            UpperBound = expectedValue + ((expectedValue / 100) * range);
        }
    }
}
