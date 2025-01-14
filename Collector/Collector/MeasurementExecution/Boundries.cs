using Collector.Communication.DataModel;

namespace Collector.MeasurementExecution
{
    internal class Boundries
    {
        private int m_NegativeFailurePercentage;
        private int m_PositiveFailurePercentage;

        public Types Type { get; private set; }
        public int ExpectedValue { get; private set; }
        public int UpperBoundry { get; private set; }
        public int LowerBoundry { get; private set; }
    
        public Boundries(Types type, int expected, int negativeFailure, int positiveFailure) {
            Type = type;
            m_NegativeFailurePercentage = negativeFailure;
            m_PositiveFailurePercentage = positiveFailure;
            updateExpectedValue(expected);
        }

        public void updateExpectedValue(int newExpectedValue)
        {
            ExpectedValue = newExpectedValue;
            LowerBoundry = ExpectedValue - ((ExpectedValue / 100) * m_NegativeFailurePercentage);
            UpperBoundry = ExpectedValue + ((ExpectedValue / 100) * m_PositiveFailurePercentage);
        }
    }
}
