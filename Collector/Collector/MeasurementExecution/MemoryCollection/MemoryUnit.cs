using Collector.Communication;
using Collector.Communication.DataModel;
using NLog;
using System;
using System.Collections.Generic;
using System.Timers;
using System.Globalization;
using Timer = System.Timers.Timer;
using System.Diagnostics;
using NPOI.SS.Formula.Functions;
using System.Runtime.InteropServices;

namespace Collector.MeasurementExecution.MemoryCollection
{
    internal class MemoryUnit : IMeasurmenetUnit
    {
        #region Variables

        private Logger m_Logger;
        private PerformanceCounter m_ramCounter;

        private int m_ElapsedTimer = 0;
        private Timer m_Timer;
        private object m_LockObject = new object();

        #endregion

        #region Event

        public event EventHandler<MeasurementEvent> OnMeasurmentHappens;

        #endregion

        #region Constructor

        public MemoryUnit(Logger logger)
        {
            m_Logger = logger;

            if (RuntimeInformation.IsOSPlatform(OSPlatform.Windows))
            {
                m_ramCounter = new PerformanceCounter("Memory", "Available MBytes");
            } 
            else if(RuntimeInformation.IsOSPlatform(OSPlatform.Linux))
            {
                //TODO:  ADD LINUX VERSION
            }
            StartTimer(10000);
        }

        #endregion

        #region public methods

        public void StopMeasurement()
        {
            m_Timer.Stop();
        }

        #endregion

        #region Eventhandler

        // Specify what you want to happen when the Elapsed event is raised.
        private void OnTimedEvent(object source, ElapsedEventArgs e)
        {

            var id = NextCounter();
            var measurement = new RamMeasurement();

            if (RuntimeInformation.IsOSPlatform(OSPlatform.Linux))
            {
                var gc = GC.GetGCMemoryInfo();
                measurement.AvailableMemory = (int) gc.TotalAvailableMemoryBytes;
            }
            else if(RuntimeInformation.IsOSPlatform(OSPlatform.Windows))
            {
                measurement.AvailableMemory = (int)m_ramCounter.NextValue();
            }
            var dateTime = DateTime.Now;
            measurement.Date = dateTime.ToString("d");
            measurement.Time = dateTime.ToString("HH:mm:ss.fff", CultureInfo.CurrentCulture);

            measurement.Id = id;

            NotifyListeners(measurement);

            // TODO: Checken, ob wir hier abbrechen m�ssen
        }

        #endregion

        #region private methods

        private int NextCounter()
        {
            int nextCounter;
            lock (m_LockObject)
            {
                m_ElapsedTimer++;
                nextCounter = m_ElapsedTimer;
            }
            return nextCounter;
        }

        private void StartTimer(int interval)
        {
            m_Timer = new Timer();

            m_Timer.Elapsed += new ElapsedEventHandler(OnTimedEvent);
            m_Timer.Interval = interval;
            m_Timer.Enabled = true;

            m_Timer.Start();
        }

        #endregion

        #region Handle listeners

        public void AddListener(SubscriptionLifecycle listener)
        {
            //Only relevant for timer (Store needed intervall to calculate ggT)
        }

        public void RemoveListener(SubscriptionLifecycle listener)
        {
            //Only relevant for timer (Store needed intervall to calculate ggT)
        }

        public void UpdateListener(SubscriptionLifecycle listener)
        {
            //Only relevant for timer (Store needed intervall to calculate ggT)
        }

        private void NotifyListeners(RamMeasurement measurement)
        {
            var measurementEvent = new MeasurementEvent();
            measurementEvent.Ram = measurement;
            OnMeasurmentHappens?.Invoke(this, measurementEvent);
        }

        #endregion
    }
}
