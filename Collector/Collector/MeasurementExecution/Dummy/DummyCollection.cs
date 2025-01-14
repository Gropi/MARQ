using Collector.Communication.DataModel;
using Encapsulation.Simulation;
using System;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace Collector.MeasurementExecution.Dummy
{
    internal class DummyCollection : IMeasurmenetUnit
    {
        private List<SimulatedParameter> m_SimulatedParameters;
        private bool m_IsRunning = false;

        public event EventHandler<MeasurementEvent> OnMeasurmentHappens;

        public DummyCollection()
        {
            m_SimulatedParameters = new List<SimulatedParameter>();
        }

        public void AddListener(SubscriptionLifecycle listener)
        {
            throw new NotImplementedException();
        }

        public void RemoveListener(SubscriptionLifecycle listener)
        {
            throw new NotImplementedException();
        }

        public void UpdateListener(SubscriptionLifecycle listener)
        {
            throw new NotImplementedException();
        }

        public async Task updateSetupParameters()
        {
            if (!m_IsRunning)
            {
                m_IsRunning = true;
                while (m_IsRunning)
                {
                    foreach (var parameter in m_SimulatedParameters)
                    {
                        if (parameter == null)
                            continue;
                        if (parameter.SimulationType.Equals(Types.Latency))
                            HandleNetwork(parameter);
                        if (parameter.SimulationType.Equals(Types.Ram))
                            HandleRAM(parameter);
                        if (parameter.SimulationType.Equals(Types.Cpu))
                            HandleCPU(parameter);

                    }
                    await Task.Delay(10);
                }
            }
        }

        public async Task HandleSetupDummy(TestSetupMessage message)
        {
            foreach (var testSetup in message.SimulatedParameters)
            {
                if (testSetup == null)
                    continue;
                m_SimulatedParameters.Add(new SimulatedParameter(testSetup.ParameterType, testSetup.ExpectedValue, testSetup.PositivePercentage));
            }
        }

        public void StopSending()
        {
            m_IsRunning = false;
        }

        private void HandleNetwork(SimulatedParameter parameter)
        {
            var random = new Random().Next(parameter.LowerBound, parameter.UpperBound + 1);
            var measurement = new NetworkMeasurement();
            measurement.Rtt = random;
            NotifyListeners(measurement);
        }

        private void HandleRAM(SimulatedParameter parameter)
        {
            var random = new Random().Next(parameter.LowerBound, parameter.UpperBound + 1);
            var measurement = new RamMeasurement();
            measurement.AvailableMemory = random;
            NotifyListeners(measurement);
        }

        private void HandleCPU(SimulatedParameter parameter)
        {
            var random = new Random().Next(parameter.LowerBound, parameter.UpperBound + 1);
            var measurement = new CpuMeasurement();
            measurement.CpuUsage = random;
            NotifyListeners(measurement);
        }

        private void NotifyListeners(CpuMeasurement measurement)
        {
            var measurementEvent = new MeasurementEvent();
            measurementEvent.Cpu = measurement;
            OnMeasurmentHappens?.Invoke(this, measurementEvent);
        }

        private void NotifyListeners(NetworkMeasurement measurement)
        {
            var measurementEvent = new MeasurementEvent();
            measurementEvent.Network = measurement;
            OnMeasurmentHappens?.Invoke(this, measurementEvent);
        }

        private void NotifyListeners(RamMeasurement measurement)
        {
            var measurementEvent = new MeasurementEvent();
            measurementEvent.Ram = measurement;
            OnMeasurmentHappens?.Invoke(this, measurementEvent);
        }
    }
}
