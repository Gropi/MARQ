using System;
using System.Threading.Tasks;

namespace Collector.Simulation
{
    internal class ConsoleInputReader
    {
        private bool m_Running;

        public ConsoleInputReader() { 
            m_Running = true;
            Task.Run(() => readConsole());
        }

        public event EventHandler<string> InputRecieved;

        public void readConsole()
        {
            while(m_Running) 
            {
                var input = Console.ReadLine();
                if (input != null)
                    OnInputReceived(input);
            }
        }

        public void stop()
        {
            m_Running = false;
        }

        private void OnInputReceived(string input)
        {
            InputRecieved?.Invoke(this, input);
        }
    }
}
