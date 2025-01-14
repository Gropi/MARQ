using Collector.Communication.DataModel;
using CommonLibrary.Communication.DataModel;

namespace Collector.MeasurementExecution.DataModel
{
    internal class MeasurementListener
    {
        public Subscribe SubscriptionInformation { get; set; }

        public ConnectionInformation ConnectionInformation { get; set; }
    }
}
