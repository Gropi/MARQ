using Collector.Communication.DataModel;
using System;

namespace Collector.MeasurementExecution
{
    internal interface IMeasurmenetUnit
    {
        #region Event

        event EventHandler<MeasurementEvent> OnMeasurmentHappens;

        #endregion

        #region Listeners

        void AddListener(SubscriptionLifecycle listener);

        void RemoveListener(SubscriptionLifecycle listener);

        void UpdateListener(SubscriptionLifecycle listener);

        #endregion
    }
}
