using Collector.Communication.DataModel;
using NLog;
using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;

namespace Encapsulation.Communication.DataModel
{
    internal class VectorClock
    {
        private Logger m_ApplicationLogger;
        public ConcurrentDictionary<string, int> Clock { get; private set; } = new ConcurrentDictionary<string, int>();
        public string ProcessKey { get; private set; }

        public VectorClock(Logger applicationLogger, string currentProcessKey) 
        {
            m_ApplicationLogger = applicationLogger;
            ProcessKey = currentProcessKey;
            Clock.AddOrUpdate(currentProcessKey, 0, (k, v) => 0);
        } 

        public VectorClock(Logger applicationLogger, string currentProcessKey, VectorClock other) : this(applicationLogger, currentProcessKey) 
        {
            AddRange(other);
        }

        public void AddRange(VectorClock clock)
        {
            if (clock == null)
                throw new ArgumentNullException(nameof(clock));
            foreach (var element in clock.Clock)
                Clock.AddOrUpdate(element.Key, element.Value, (k, v) => element.Value);
        }

        public void Increase()
        {
            Clock.AddOrUpdate(ProcessKey, 0, (k, v) => v + 1);
        }

        public bool IsNewer(VectorClock other)
        {
            var key = other.ProcessKey;

            if (!Clock.ContainsKey(key))
            {
                return true;
            }
            else
            {
                return Clock[key] < other.Clock[key];
            }
        }

        public bool IsNewer(string key, List<ProcessState> states)
        {
            var singleState = states.FirstOrDefault(x => x.Key.Equals(key));

            if (singleState != null)
            {
                var currentValue = Clock.GetValueOrDefault(key);
                return currentValue < singleState.Value;
            }
            return false;
        }

        public List<ProcessState> ToProcessState()
        {
            var processStates = new List<ProcessState>();
            foreach (var entry in Clock.Keys)
            {
                // #SucheFreunde...
                var processState = new ProcessState();
                // #LadeFreunde...
                processState.Key = entry;
                processState.Value = Clock[entry];
                processStates.Add(processState);
                // #KeinenGefunden---Sorry...
            }
            return processStates;
        }

        public void AddSingleProcessState(string key, List<ProcessState> states)
        {
            var singleState = states.FirstOrDefault(x => x.Key.Equals(key));
            var currentValue = Clock.GetValueOrDefault(key);

            if (currentValue == null || currentValue < singleState.Value)
            {
                Clock.AddOrUpdate(key, singleState.Value, (k, v) => { return currentValue; });
            }
        }

        public void AddProcessStates(List<ProcessState> processStates)
        {
            foreach (var entry in processStates)
            {
                var key = entry.Key;
                var value = entry.Value;

                Clock.AddOrUpdate(key, value, (k, v) => {
                    return v > value ? v : value;
                });
            }
        }

        public void AddVectorClock(VectorClock other)
        {
            foreach (var key in other.Clock.Keys)
            {
                var value = other.Clock[key];

                Clock.AddOrUpdate(key, value, (k, v) => {
                    return v > value ? v : value;
                });
            }
        }
    }
}
