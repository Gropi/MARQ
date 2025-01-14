using CommonLibrary.Communication.Facade;
using Encapsulation.Businesslogic;
using Encapsulation.Helper.impl;
using NLog;
using System;
using System.Linq;

namespace Encapsulation
{
    class Program
    {
        #region static variables

        private static Logger m_Logger = LogManager.GetLogger("applicationLogger");
        private static int m_Port = 2002;
        private static string m_Endpoint = "";
        private static string m_ID = "";

        #endregion

        #region Main 

        public static void Main(string[] args)
        {
            try
            {
                // first we want to specify the one we want to ping
                ParseArgs(args);

                var communicationHelper = new CommunicationHelper(m_Logger, m_ID);
                var communicationFacade = new CommunicationFacade(m_Logger);

                if (args.Contains("ts"))
                {
                    m_Logger.Info("Choosen container: Torchserve");
                    var businesslogic = new TSEncapsulationBL(m_Port, m_Endpoint, m_Logger, communicationHelper, communicationFacade);
                }
                else if (args.Contains("fr"))
                {
                    m_Logger.Info("Choosen container: Face recognition");
                    var businesslogic = new FREncapsulationBL(m_Port, m_Endpoint, m_Logger, communicationHelper, communicationFacade);
                }
                else if (args.Contains("dep"))
                {
                    m_Logger.Info("Choosen container: Deployer");
                    var businesslogic = new DeployerBL(m_Port, m_Logger, communicationHelper, communicationFacade);
                }
                else if (args.Contains("blur"))
                {
                    m_Logger.Info("Choosen container: Blurring");
                    var businesslogic = new MagickBlurringBL(m_Port, m_Logger, communicationHelper, communicationFacade);
                }
                else if (args.Contains("dummy"))
                {
                    m_Logger.Info("Choosen container: Dummy (der)");
                    var businesslogic = new DummyExecutionBL(m_Port, m_Logger, communicationHelper, communicationFacade);
                }
                else
                {
                    m_Logger.Error("Choose which services to encapsulate.");
                }
            } catch (Exception ex) 
            {
                Console.WriteLine(ex.ToString());
                m_Logger.Error("Exception thrown: ", ex);
            }
        }

        #endregion

        private static void ParseArgs(string[] args)
        {
            for (int i = 0; i < args.Length; i++)
            {
                var arg = args[i].ToLower();
                if (arg.Equals("-p"))
                {
                    i++;
                    if (args.Length > i)
                        m_Port = Int32.Parse(args[i]);
                    else
                        m_Logger.Error("Missing parameter -p");
                }
                if (arg.Equals("-ep"))
                {
                    i++;
                    if (args.Length > i)
                        m_Endpoint = args[i];
                    else
                        m_Logger.Error("Missing parameter -ep");
                }
                if (arg.Equals("-id"))
                {
                    i++;
                    if (args.Length > i)
                        m_ID = args[i];
                    else
                        m_Logger.Error("Missing parameter -id");
                }
            }
        }
    }
}
