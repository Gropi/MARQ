using Collector.Communication.DataModel;
using NLog;
using System;
using System.Diagnostics;
using System.Globalization;
using System.Runtime.InteropServices;
using System.Timers;
using Timer = System.Timers.Timer;

namespace Collector.MeasurementExecution.CpuCollection
{
    internal class CpuUnit : IMeasurmenetUnit
    {
        #region Variables

        private Logger m_Logger;
        private PerformanceCounter m_cpuCounter;

        private int m_ElapsedTimer = 0;
        private Timer m_Timer;
        private object m_LockObject = new object();

        private DateTime m_startTime;
        private TimeSpan m_startCpuUsage;

        #endregion

        #region Event

        public event EventHandler<MeasurementEvent> OnMeasurmentHappens;

        #endregion

        #region Constructor

        public CpuUnit(Logger logger)
        {
            m_Logger = logger;

            if (RuntimeInformation.IsOSPlatform(OSPlatform.Linux))
            {
                m_startTime = DateTime.UtcNow;
                m_startCpuUsage = getProcessorTimes();
            }
            else
            {
                m_cpuCounter = new PerformanceCounter("Processor", "% Processor Time", "_Total");
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
            var measurement = new CpuMeasurement();        

            if (RuntimeInformation.IsOSPlatform(OSPlatform.Linux))
            {
                var endTime = DateTime.UtcNow;
                var endCpuUsage = getProcessorTimes();

                var cpuUsedMs = (endCpuUsage - m_startCpuUsage).TotalMilliseconds;
                var totalMsPassed = (endTime - m_startTime).TotalMilliseconds;
                var cpuUsageTotal = cpuUsedMs / (Environment.ProcessorCount * totalMsPassed);

                measurement.CpuUsage = cpuUsageTotal;

                m_startCpuUsage = endCpuUsage;
                m_startTime = endTime;
            }
            else
            {
                measurement.CpuUsage = (int)m_cpuCounter.NextValue();
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

        private TimeSpan getProcessorTimes()
        {
            TimeSpan time = TimeSpan.Zero;
            Process[] processes = Process.GetProcesses();

            for(int i = 0; i < processes.Length; i++)
            {
                if (processes[i].Id == 0)
                    continue;
                time.Add(processes[i].TotalProcessorTime);
            }

            return time;
        }

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

        private void NotifyListeners(CpuMeasurement measurement)
        {
            var measurementEvent = new MeasurementEvent();
            measurementEvent.Cpu = measurement;
            OnMeasurmentHappens?.Invoke(this, measurementEvent);
        }

        #endregion
    }
}
