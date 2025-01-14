using NLog;
using NPOI.HSSF.UserModel;
using NPOI.SS.UserModel;
using NPOI.XSSF.UserModel;
using CommonLibrary.IO;
using CommonLibrary.Properties;
using Collector.Parser.Objects;

namespace CommonLibrary.Parser.impl
{
    public class ExcelParser : IExcelParser
    {
        #region Variables

        public const char CSVPARSER_COMMA_SEPERATOR = ',';
        public const char CSVPARSER_SEMICOLON_SEPARATOR = ';';
        private const string XLS_EXTENSION = ".xls";
        private const string CSV_EXTENSION = ".csv";
        private const string XLSX_EXTENSION = ".xlsx";
        private IOHandler m_IOHandler;
        private ILogger m_Logger;

        #endregion

        #region Properties

        public char CSVSeperator { get; set; }

        #endregion

        #region Constructor

        public ExcelParser(IOHandler ioHandler, ILogger logger)
        {
            m_IOHandler = ioHandler;
            CSVSeperator = CSVPARSER_SEMICOLON_SEPARATOR;
            m_Logger = logger;
        }

        #endregion

        #region Helper methods

        private bool HandleFileOnIO(string filepath, string expectedFileExtension)
        {
            if (filepath is null)
                throw new IOException(Resources.FILE_PATH_NULL);
            if (expectedFileExtension is null)
                throw new IOException(Resources.EXPECTED_FILE_EXTENSION_NULL);
            if (!m_IOHandler.FileExists(filepath))
                throw new FileNotFoundException(Resources.IOHANDLER_FILE_NOT_FOUND, filepath);
            if (!filepath.EndsWith(expectedFileExtension))
                throw new Exception(Resources.NOT_CORRECT_FORMAT);
            return true;
        }

        #endregion

        #region Handle CSV file

        public IList<string[]> ParseCSV(string fileNameWithPath)
        {
            try
            {
                var csvContent = new List<string[]>();
                if (HandleFileOnIO(fileNameWithPath, CSV_EXTENSION))
                {
                    m_Logger.Info(string.Format(Resources.EXCEL_PARSER_START_LOADING_FILE, fileNameWithPath));
                    using (var reader = new StreamReader(fileNameWithPath))
                    {
                        while (!reader.EndOfStream)
                        {
                            var line = reader.ReadLine();
                            csvContent.Add(line.Split(CSVSeperator));
                        }
                        m_Logger.Info(Resources.EXCEL_PARSER_CSV_SUCCESSFULY_GENERATED, true);
                    }
                }
                return csvContent;
            }
            catch (Exception ex)
            {
                HandleLoggingOfException(ex);
                throw;
            }
        }

        public void AppendToCSVFile(string[] content, string filePath)
        {
            try
            {
                if (content is null)
                    throw new IOException(Resources.CONTENT_LIST_NULL);
                if (HandleFileOnIO(filePath, CSV_EXTENSION))
                {
                    var contentAsList = new List<string[]>();
                    contentAsList.Add(content);
                    WriteCSVFile(contentAsList, filePath, true, false);
                    m_Logger.Info(Resources.CSVFILE_APPENDED_SUCCESSFULY_TO_CONTENT, true);
                }
            }
            catch (Exception ex)
            {
                HandleLoggingOfException(ex);
                throw;
            }
        }

        public void WriteCSVFile(IList<string[]> content, string filePath, bool appendText)
        {
            try
            {
                if (content is null)
                    throw new IOException(Resources.CONTENT_LIST_NULL);
                if (HandleFileOnIO(filePath, CSV_EXTENSION))
                {
                    WriteCSVFile(content, filePath, appendText, false);
                    m_Logger.Info(Resources.EXCEL_PARSER_CSV_SUCCESSFULY_GENERATED, true);
                }
            }
            catch (Exception ex)
            {
                HandleLoggingOfException(ex);
                throw;
            }
        }

        public void WriteCSVFile(IList<string[]> content, string filePath, bool appendText, bool overrideFile)
        {
            try
            {
                if (filePath is null)
                    throw new IOException(Resources.FILE_PATH_NULL);
                if (m_IOHandler.FileExists(filePath) && !appendText && !overrideFile)
                    throw new Exception(Resources.OVERRIDE_FILE_DISABLED);
                if (overrideFile)
                    m_IOHandler.DeleteFile(filePath);
                else if (m_IOHandler.FileExists(filePath) && !appendText)
                    throw new Exception(Resources.APPEND_TEXTNOT_ALLOWED);
                using (var writer = File.AppendText(filePath))
                {
                    foreach (string[] line in content)
                    {
                        for (int i = 0; i < line.Length; i++)
                        {
                            writer.Write(line[i]);
                            if (i + 1 != line.Length)
                                writer.Write(CSVSeperator);
                        }
                        writer.Write(writer.NewLine);
                    }
                    m_Logger.Info(Resources.EXCEL_PARSER_CSV_SUCCESSFULY_GENERATED, true);
                }
            }
            catch (Exception ex)
            {
                HandleLoggingOfException(ex);
                throw;
            }
        }

        public void WriteCSVFile(IDictionary<string, string> parameters, string fileToSaveTo, bool overrideFile)
        {
            try
            {
                if (parameters is null)
                    throw new ArgumentNullException(Resources.PARAMETER_IS_NULL, "parameters");
                var content = new List<string[]>();
                foreach (var key in parameters.Keys)
                {
                    var line = new string[] { key, parameters[key] };
                    content.Add(line);
                }
                WriteCSVFile(content, fileToSaveTo, false, overrideFile);
                m_Logger.Info(Resources.EXCEL_PARSER_CSV_SUCCESSFULY_GENERATED, true);
            }
            catch (Exception ex)
            {
                HandleLoggingOfException(ex);
                throw;
            }
        }

        public void WriteToCSV<T>(IList<T> entries, string filePath, bool appendText, bool overrideFile) where T : IExcelParsable
        {
            try
            {
                if (filePath is null)
                    throw new ArgumentNullException(Resources.FILE_PATH_NULL);
                if (m_IOHandler.FileExists(filePath) && !appendText && !overrideFile)
                    throw new Exception(Resources.OVERRIDE_FILE_DISABLED);
                if (overrideFile)
                    m_IOHandler.DeleteFile(filePath);
                else if (m_IOHandler.FileExists(filePath) && !appendText)
                    throw new Exception(Resources.APPEND_TEXTNOT_ALLOWED);

                using (var writer = File.AppendText(filePath))
                {
                    foreach (var entry in entries)
                    {
                        var line = entry.GetObjectAsStringArray();

                        for (int i = 0; i < line.Length; i++)
                        {
                            writer.Write(line[i]);
                            if (i + 1 != line.Length)
                                writer.Write(CSVSeperator);
                        }
                        writer.Write(writer.NewLine);
                    }
                    m_Logger.Info(Resources.EXCEL_PARSER_CSV_SUCCESSFULY_GENERATED, true);
                }
            }
            catch (Exception ex)
            {
                HandleLoggingOfException(ex);
                throw;
            }
        }

        public void WriteToCSV<T>(IList<T> entries, string[] columnHeader, string filePath, bool appendText, bool overrideFile) where T : IExcelParsable
        {
            try
            {
                if (filePath is null)
                    throw new IOException(Resources.FILE_PATH_NULL);
                if (m_IOHandler.FileExists(filePath) && !appendText && !overrideFile)
                    throw new Exception(Resources.OVERRIDE_FILE_DISABLED);
                if (overrideFile)
                    m_IOHandler.DeleteFile(filePath);
                else if (m_IOHandler.FileExists(filePath) && !appendText)
                    throw new Exception(Resources.APPEND_TEXTNOT_ALLOWED);

                using (var writer = File.AppendText(filePath))
                {
                    foreach (var columnName in columnHeader)
                    {
                        writer.Write(columnName);
                        writer.Write(CSVSeperator);
                    }

                    writer.Write(writer.NewLine);

                    foreach (var entry in entries)
                    {
                        var line = entry.GetObjectAsStringArray();

                        for (int i = 0; i < line.Length; i++)
                        {
                            writer.Write(line[i]);
                            if (i + 1 != line.Length)
                                writer.Write(CSVSeperator);
                        }
                        writer.Write(writer.NewLine);
                    }
                    m_Logger.Info(Resources.EXCEL_PARSER_CSV_SUCCESSFULY_GENERATED, true);
                }
            }
            catch (Exception ex)
            {
                HandleLoggingOfException(ex);
                throw;
            }
        }

        #endregion

        #region General sheet handler

        private IList<string[]> GetAllEntriesOfSheet(ISheet sheet)
        {
            var entries = new List<string[]>();
            if (sheet.LastRowNum == 0)
                return entries;
            for (int r = 0; r <= sheet.LastRowNum; r++)
            {
                var row = sheet.GetRow(r);
                if (row != null)
                {
                    var entryRow = new string[row.LastCellNum];
                    for (int c = 0; c < row.LastCellNum; c++)
                    {
                        entryRow[c] = row.GetCell(c) != null ? row.GetCell(c).ToString() : "";
                    }
                    entries.Add(entryRow);
                }
            }
            return entries;
        }

        #endregion

        #region Parse and write XLS

        public IList<string[]> ParseXLS(string filepath)
        {
            try
            {
                if (HandleFileOnIO(filepath, XLS_EXTENSION))
                {
                    using (var file = new FileStream(filepath, FileMode.Open, FileAccess.Read))
                    {
                        m_Logger.Info(Resources.EXCEL_PARSER_FILESTREAM_SUCCESSFULY_GENERATED, true);
                        return ParseXLS(file);
                    }
                }
            }
            catch (Exception ex)
            {
                HandleLoggingOfException(ex);
                throw;
            }
            return null;
        }

        public IList<string[]> ParseXLS(FileStream fileStream)
        {
            IList<string[]> allEntries = new List<string[]>();
            try
            {
                if (fileStream is null)
                    throw new IOException(Resources.FILE_STREAM_NULL);
                HSSFWorkbook hSSFWorkbook = new HSSFWorkbook(fileStream);
                ISheet sheet = hSSFWorkbook.GetSheetAt(0);
                allEntries = GetAllEntriesOfSheet(sheet);
                m_Logger.Info(Resources.EXCEL_PARSER_XLS_SUCCESSFULY_GENERATED, true);
            }
            catch (Exception ex)
            {
                HandleLoggingOfException(ex);
                throw;
            }
            return allEntries;
        }

        #endregion 

        #region Parse and write XLSX 

        public IDictionary<string, IList<string[]>> ReadXLSXFile(string fileToReadFrom, int indexOfSheet)
        {
            try
            {
                var allSheetsWithEntries = new Dictionary<string, IList<string[]>>();
                if (HandleFileOnIO(fileToReadFrom, XLSX_EXTENSION))
                {
                    m_Logger.Info(string.Format(Resources.EXCEL_PARSER_START_LOADING_FILE, fileToReadFrom));
                    using (var file = new FileStream(fileToReadFrom, FileMode.Open, FileAccess.Read))
                    {
                        m_Logger.Info(Resources.EXCEL_PARSER_FILESTREAM_SUCCESSFULY_GENERATED, true);
                        var workbook = new XSSFWorkbook(file);

                        allSheetsWithEntries.Add(workbook.GetSheetName(indexOfSheet), ReadXLSXFile(workbook, indexOfSheet));
                    }
                }
                return allSheetsWithEntries;
            }
            catch (Exception ex)
            {
                HandleLoggingOfException(ex);
                throw;
            }
        }

        public IDictionary<string, IList<string[]>> ReadXLSXFile(string fileToReadFrom, string sheetName)
        {
            try
            {
                var allSheetsWithEntries = new Dictionary<string, IList<string[]>>();
                if (HandleFileOnIO(fileToReadFrom, XLSX_EXTENSION))
                {
                    m_Logger.Info(string.Format(Resources.EXCEL_PARSER_START_LOADING_FILE, fileToReadFrom));
                    using (var file = new FileStream(fileToReadFrom, FileMode.Open, FileAccess.Read))
                    {
                        m_Logger.Info(Resources.EXCEL_PARSER_FILESTREAM_SUCCESSFULY_GENERATED, true);
                        var workbook = new XSSFWorkbook(file);

                        allSheetsWithEntries.Add(sheetName, ReadXLSXFile(workbook, sheetName));
                    }
                }
                return allSheetsWithEntries;
            }
            catch (Exception ex)
            {
                HandleLoggingOfException(ex);
                throw;
            }
        }

        public IDictionary<string, IList<string[]>> ReadXLSXFile(string fileToReadFrom)
        {
            try
            {
                var allSheetsWithEntries = new Dictionary<string, IList<string[]>>();
                if (HandleFileOnIO(fileToReadFrom, XLSX_EXTENSION))
                {
                    m_Logger.Info(string.Format(Resources.EXCEL_PARSER_START_LOADING_FILE, fileToReadFrom));
                    using (var file = new FileStream(fileToReadFrom, FileMode.Open, FileAccess.Read))
                    {
                        m_Logger.Info(Resources.EXCEL_PARSER_FILESTREAM_SUCCESSFULY_GENERATED, true);
                        var workbook = new XSSFWorkbook(file);

                        for (int i = 0; i < workbook.Count; i++)
                        {
                            allSheetsWithEntries.Add(workbook.GetSheetName(i), ReadXLSXFile(workbook, i));
                        }
                    }
                }
                return allSheetsWithEntries;
            }
            catch (Exception ex)
            {
                HandleLoggingOfException(ex);
                throw;
            }
        }

        public void WriteXLSXFile(string fileToWriteTo)
        {

        }

        private IList<string[]> ReadXLSXFile(XSSFWorkbook fileToReadFrom, int indexOfSheet)
        {
            IList<string[]> entries = new List<string[]>();

            if (fileToReadFrom.Count > indexOfSheet)
            {
                var sheet = fileToReadFrom.GetSheetAt(indexOfSheet);
                entries = GetAllEntriesOfSheet(sheet);
            }

            return entries;
        }

        private IList<string[]> ReadXLSXFile(XSSFWorkbook fileToReadFrom, string sheetName)
        {
            var index = fileToReadFrom.GetSheetIndex(sheetName);
            return ReadXLSXFile(fileToReadFrom, index);
        }

        #endregion

        #region Errorhandling

        private void HandleLoggingOfException(Exception ex)
        {
            if (ex is IOException)
                m_Logger.Error(Resources.EXCELPARSER_HANDLED_IO_EXCEPTION, ex, true);
            else
                m_Logger.Error(string.Format(Resources.ERROR, ex, true));
            Console.WriteLine(ex.ToString());
        }

        #endregion
    }
}
