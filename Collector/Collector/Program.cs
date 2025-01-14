using Collector.Businesslogic;
using Collector.Properties;
using CommonLibrary.Communication.DataModel;
using CommonLibrary.Communication.Facade;
using CommonLibrary.IO.impl;
using NLog;
using System;

namespace Collector
{
    class Program
    {
        #region static variables
        
        private static Logger m_Logger;
        private static int m_CollectorPort;
        private static int m_HandlerPort;
        private static ConnectionInformation m_TargetConnection;
        private static bool m_StartCollector;
        private static bool m_StartDockerhandler;
        private static string m_ID;
        private static string m_ContainerMode;
        private static int m_DummyContainerCount;
        private static int m_DummyContainerStartIndex;

        #endregion

        #region Main 

        public static void Main(string[] args)
        {
            m_Logger = LogManager.GetLogger("applicationLogger");

            m_ContainerMode = "dummy";
            m_DummyContainerCount = 10;
            m_DummyContainerStartIndex = 1;

            m_StartCollector = false;
            m_StartDockerhandler = false;

            // STANDART MANAGEMENT PORT
            m_CollectorPort = 2003;
            m_HandlerPort = 2001;

            m_TargetConnection = new ConnectionInformation();
            var communicationFacade = new CommunicationFacade(m_Logger);

            // first we want to specify the one we want to ping
            ParseArgs(args);

            if(m_StartCollector)
            {
                var ioHandler = new IOHandlerImpl(m_Logger);
                new CollectorBusinesslogic(m_CollectorPort, ioHandler, m_ID, m_Logger, communicationFacade);
            }
            else if (m_StartDockerhandler)
            {
                new ContainerHandlingLogic(m_HandlerPort, m_TargetConnection, m_ContainerMode, m_DummyContainerCount, m_Logger, communicationFacade, m_DummyContainerStartIndex);
            } 
            else
            {
                LogError("Invalid arguments. Choose mode to run.");
            }
            m_Logger.Info("Close applicaiton.");
        }

        # endregion

        private static void ParseArgs(string[] args)
        {
            for (int i = 0; i < args.Length; i++)
            {
                var arg = args[i].ToLower();
                if (arg.Equals("-p"))
                {
                    i++;
                    if (args.Length > i)
                    {
                        m_CollectorPort = Int32.Parse(args[i]);
                        m_HandlerPort = Int32.Parse(args[i]);
                    }
                    else
                        LogError(string.Format(Resources.ARGS_MISSING_PARAMETER, "-p"));
                }
                else if (arg.Equals("serveandcollect"))
                {
                    m_StartCollector = true;
                }
                else if (arg.Equals("handlecontainers"))
                {
                    m_StartDockerhandler = true;
                }
                else if (arg.Equals("-t"))
                {
                    i++;
                    if (args.Length > i)
                    {
                        m_TargetConnection.Address = args[i];
                    }
                    else
                        LogError(string.Format(Resources.ARGS_MISSING_PARAMETER, "-t"));
                }
                else if (arg.Equals("-tp"))
                {
                    i++;
                    if (args.Length > i)
                    {
                        m_TargetConnection.Port = Int32.Parse(args[i]);
                    }
                    else
                        LogError(string.Format(Resources.ARGS_MISSING_PARAMETER, "-tp"));
                }
                else if (arg.Equals("-id"))
                {
                    i++;
                    if (args.Length > i)
                    {
                       m_ID = args[i];
                    }
                    else
                        LogError(string.Format(Resources.ARGS_MISSING_PARAMETER, "-id"));
                }
                else if (arg.Equals("-md"))
                {
                    i++;
                    if (args.Length > i)
                    {
                        m_ContainerMode = args[i];
                    }
                    else
                        LogError(string.Format(Resources.ARGS_MISSING_PARAMETER, "-md"));
                }
                else if (arg.Equals("-dc"))
                {
                    i++;
                    if (args.Length > i)
                        m_DummyContainerCount = int.Parse(args[i]);
                    else
                        LogError(string.Format(Resources.ARGS_MISSING_PARAMETER, "-dc"));

                }
                // Dummy container start index. Default: 1
                else if (arg.Equals("-dcsi"))
                {
                    i++;
                    if (args.Length > i)
                        m_DummyContainerStartIndex = int.Parse(args[i]);
                    else
                        LogError(string.Format(Resources.ARGS_MISSING_PARAMETER, "-dcsi"));

                }
            }
        }

        private static void LogError(string error)
        {
            m_Logger.Error(error);
            Console.Error.WriteLine(error);
        }
    }
}