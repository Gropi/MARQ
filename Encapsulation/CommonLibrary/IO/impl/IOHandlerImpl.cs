using CommonLibrary.Properties;
using NLog;
using System.Reflection;

namespace CommonLibrary.IO.impl
{
    public class IOHandlerImpl : IOHandler
    {
        #region Class variables

        private ILogger m_Logger;

        #endregion

        #region Constructor

        public IOHandlerImpl(ILogger logger)
        {
            m_Logger = logger;
        }

        #endregion

        #region Properties
        public string GetCurrentApplicationFolder
        {
            get
            {
                var assembly = Assembly.GetExecutingAssembly();
                return Directory.GetParent(assembly.Location).FullName;
            }
        }

        #endregion

        public string[] GetFileNamesOfDirectory(string directoryName)
        {
            try
            {
                return Directory.GetFiles(directoryName);
            }
            catch (Exception ex)
            {
                HandleLoggingOfException(ex);
                throw;
            }
        }

        public FileInfo[] GetAllFileObjectsFromDirectory(string directory)
        {
            return GetAllFileObjectsByExtensionFromDirectory(directory, ".*");
        }

        public FileInfo[] GetAllFileObjectsFromDirectory(string directory, bool recursive)
        {
            return GetAllFileObjectsByExtensionFromDirectory(directory, recursive, ".*");
        }

        public FileInfo[] GetAllFileObjectsByExtensionFromDirectory(string directory, string extension)
        {
            if (directory == null || extension == null)
            {
                var nullParameter = directory == null ? "directory" : "extension";
                var errorMessage = String.Format(Resources.PARAMETER_IS_NULL, nullParameter);
                m_Logger.Error(errorMessage);
                throw new ArgumentNullException(errorMessage);
            }
            else if (!IsDirecotry(directory))
            {
                var errorMessage = String.Format(Resources.DIRECTORY_NOT_FOUND, directory);
                m_Logger.Error(errorMessage);
                throw new DirectoryNotFoundException(errorMessage);
            }
            else if (!IsExtentionOk(extension))
            {
                var errorMessage = String.Format(Resources.IOHANDLER_FILEEXTENSION_NOT_CORRECT, extension);
                m_Logger.Error(errorMessage);
                throw new IOException(errorMessage);
            }
            else
            {
                return GetAllFiles(directory, extension);
            }
        }

        public FileInfo[] GetAllFileObjectsByExtensionFromDirectory(string directory, bool recursive, string extension)
        {
            if (directory == null || extension == null)
            {
                var nullParameter = directory == null ? "directory" : "extension";
                var errorMessage = String.Format(Resources.PARAMETER_IS_NULL, nullParameter);
                m_Logger.Error(errorMessage);
                throw new ArgumentNullException(errorMessage);
            }
            else if (!IsDirecotry(directory))
            {
                var errorMessage = String.Format(Resources.DIRECTORY_NOT_FOUND, directory);
                m_Logger.Error(errorMessage);
                throw new DirectoryNotFoundException(errorMessage);
            }
            else if (!IsExtentionOk(extension))
            {
                var errorMessage = String.Format(Resources.IOHANDLER_FILEEXTENSION_NOT_CORRECT, extension);
                m_Logger.Error(errorMessage);
                throw new IOException(errorMessage);
            }
            else
            {
                return GetAllFiles(directory, recursive, extension);
            }
        }

        private FileInfo[] GetAllFiles(string directory, string extension)
        {
            try
            {
                var dict = new DirectoryInfo(directory);
                return dict.GetFiles("*" + extension);
            }
            catch (Exception ex)
            {
                HandleLoggingOfException(ex);
                throw;
            }
        }

        private FileInfo[] GetAllFiles(string directory, bool recursive, string extension)
        {
            try
            {
                var directorySearch = recursive ? SearchOption.AllDirectories : SearchOption.TopDirectoryOnly;
                var dict = new DirectoryInfo(directory);
                return dict.GetFiles("*" + extension, directorySearch);
            }
            catch (UnauthorizedAccessException ex)
            {
                m_Logger.Error(ex.Message);
                throw new UnauthorizedAccessException();
            }
            catch (Exception ex)
            {
                HandleLoggingOfException(ex);
                throw;
            }
        }

        public IList<string> GetAllFoldersFromDirectory(string destinationFolder)
        {
            if (destinationFolder == null)
            {
                var errorMessage = String.Format(Resources.PARAMETER_IS_NULL, "DesitinationFolder");
                m_Logger.Error(errorMessage);
                throw new ArgumentNullException(errorMessage);
            }
            else if (!IsDirecotry(destinationFolder))
            {
                var errorMessage = String.Format(Resources.DIRECTORY_NOT_FOUND, destinationFolder);
                m_Logger.Error(errorMessage);
                throw new DirectoryNotFoundException(errorMessage);
            }
            try
            {
                var allFolders = Directory.GetDirectories(destinationFolder);
                return new List<string>(allFolders);
            }
            catch (Exception ex)
            {
                HandleLoggingOfException(ex);
                throw;
            }
        }

        public bool CopyFileForCheckSum(string file, string targetPath, bool overwrite)
        {
            File.Copy(file, targetPath, overwrite);
            return FileExists(file);
        }

        public long GetDirectorySize(string directory, bool recursive)
        {
            return GetDirectorySize(directory, recursive, ".*");
        }

        public long GetDirectorySize(string directory, bool recursive, string fileExtension)
        {
            var directorySearch = recursive ? SearchOption.AllDirectories : SearchOption.TopDirectoryOnly;
            var fileArray = Directory.GetFiles(directory, "*" + fileExtension, directorySearch);
            long directorySize = 0;

            try
            {
                foreach (string file in fileArray)
                {
                    var info = new FileInfo(file);
                    directorySize += info.Length;
                }
            }
            catch (Exception ex)
            {
                m_Logger.Error(string.Format(Resources.IOHANDLER_ERROR_FOLDER_SIZE, ex.Message), ex);
            }
            return directorySize;
        }

        public float CalculateDirectorySize(string directory, int filesNum, int subDirNum)
        {
            try
            {
                var dirSize = 0.0f;
                var returnedDetails = new List<string>();

                if (!Directory.Exists(directory))
                    throw new DirectoryNotFoundException();
                foreach (string file in GetFileNamesOfDirectory(directory))
                {
                    var fileInfo = new FileInfo(file);
                    dirSize += fileInfo.Length;
                    filesNum++;
                }
                foreach (string subDirectory in GetAllFoldersFromDirectory(directory))
                {
                    dirSize += CalculateDirectorySize(subDirectory, filesNum, subDirNum);
                    subDirNum++;
                }
                return dirSize;
            }
            catch (Exception ex)
            {
                HandleLoggingOfException(ex);
                throw ex;
            }
        }

        public string GetPathFromApplicationWithFile(string file)
        {
            if (file is null)
                throw new IOException();
            try
            {
                var applicationPath = GetCurrentApplicationFolder;
                var files = Directory.GetFiles(applicationPath);
                var fileList = new List<String>();
                foreach (var fileInFolder in files)
                {
                    fileList.Add(fileInFolder);
                }
                return fileList.Find(s => s.Contains(file));
            }
            catch (Exception ex)
            {
                HandleLoggingOfException(ex);
                throw;
            }
        }

        public bool CreateDirectoryIfNotExisting(string directory)
        {
            if (directory is null)
                throw new IOException();
            try
            {
                if (!IsDirecotry(directory))
                    Directory.CreateDirectory(directory);
                var attr = File.GetAttributes(directory);
                return (attr & FileAttributes.Directory) == FileAttributes.Directory ? true : false;
            }
            catch (Exception ex)
            {
                HandleLoggingOfException(ex);
                throw;
            }
        }

        public bool IsDirecotry(string folder)
        {
            if (folder is null)
                throw new IOException();
            try
            {
                if (!Directory.Exists(folder))
                    return false;
                else
                    return true;
            }
            catch (Exception ex)
            {
                HandleLoggingOfException(ex);
                throw;
            }
        }

        public bool IsFile(string file)
        {
            if (file is null)
            {
                var errorMessage = String.Format(Resources.PARAMETER_IS_NULL, "file");
                m_Logger.Error(errorMessage);
                return false;
            }
            try
            {
                if (FileExists(file))
                {
                    var attr = File.GetAttributes(file);
                    return (attr & FileAttributes.Directory) == FileAttributes.Directory ? false : true;
                }
                return false;
            }
            catch (Exception ex)
            {
                HandleLoggingOfException(ex);
                throw;
            }
        }

        public bool IsExtentionOk(string extension)
        {
            if (extension is null)
                throw new IOException();
            try
            {
                return extension != null && extension.StartsWith(".");
            }
            catch (Exception ex)
            {
                HandleLoggingOfException(ex);
                throw;
            }
        }

        public string GetFileExtension(string filePath)
        {
            if (filePath is null)
                throw new IOException();
            try
            {
                string fileExtension = Path.GetExtension(filePath);
                return fileExtension;
            }
            catch (Exception ex)
            {
                HandleLoggingOfException(ex);
                throw;
            }
        }

        public string ReadAllText(string fileName)
        {
            if (fileName is null)
                throw new IOException();
            try
            {
                if (!IsFile(fileName))
                {
                    m_Logger.Error(String.Format(Resources.IOHANDLER_FILE_NOT_FOUND, fileName));
                    throw new IOException();
                }
                return File.ReadAllText(fileName);
            }
            catch (Exception ex)
            {
                HandleLoggingOfException(ex);
                throw;
            }
        }

        public string MoveFile(string source, string destination, bool overrideFile)
        {
            if (source is null || destination is null)
                throw new IOException();
            try
            {
                var fileName = PrepareCopyOrMoveOfFile(source, destination, overrideFile);
                File.Move(source, fileName);
                return fileName;
            }
            catch (Exception ex)
            {
                HandleLoggingOfException(ex);
                throw;
            }
        }

        public string CopyFile(string source, string destination, bool overrideFile)
        {
            if (source is null || destination is null)
            {
                throw new IOException();
            }
            string returnValue;
            try
            {
                var fileName = PrepareCopyOrMoveOfFile(source, destination, overrideFile);
                File.Copy(source, fileName);
                m_Logger.Info(string.Format(Resources.COPIED_SUCCESSFULY));
                returnValue = fileName;
                return returnValue;
            }
            catch (Exception ex)
            {
                m_Logger.Error(string.Format(Resources.COPY_FAILED));
                HandleLoggingOfException(ex);
                throw;
            }
        }

        public bool FileExists(string fileName)
        {
            if (fileName is null)
            {
                var errorMessage = String.Format(Resources.PARAMETER_IS_NULL, "fileName");
                m_Logger.Error(errorMessage);
                throw new ArgumentNullException(errorMessage);
            }
            try
            {
                return File.Exists(fileName);
            }
            catch (Exception ex)
            {
                HandleLoggingOfException(ex);
                throw;
            }
        }

        public string GenerateCompleteFilename(string path, string fileName)
        {
            if (path is null || fileName is null)
                throw new IOException();
            try
            {
                path = path.Replace('/', Path.DirectorySeparatorChar);
                if (!path.EndsWith(Path.DirectorySeparatorChar))
                    path += Path.DirectorySeparatorChar;
                path += fileName;
                return path;
            }
            catch (Exception ex)
            {
                HandleLoggingOfException(ex);
                throw;
            }
        }

        public bool DeleteFile(string file)
        {
            if (file is null)
                throw new IOException();
            try
            {
                if (FileExists(file))
                    File.Delete(file);
                return true;
            }
            catch (Exception ex)
            {
                m_Logger.Debug(string.Format(Resources.FILE_DELETING_FAILED, file));
                HandleLoggingOfException(ex);
                throw;
            }
        }

        private String PrepareCopyOrMoveOfFile(string source, string destination, bool overrideFile)
        {
            if (source is null || destination is null)
                throw new IOException();
            try
            {
                if (!CreateDirectoryIfNotExisting(destination))
                    throw new IOException(String.Format(Resources.DIRECTORY_NOT_FOUND, destination));
                var fileName = Path.GetFileName(source);
                var completeDestination = GenerateCompleteFilename(destination, fileName);
                if (FileExists(completeDestination))
                {
                    if (!overrideFile)
                        throw new IOException(String.Format(Resources.DIRECTORY_NOT_FOUND, completeDestination));
                    else
                        File.Delete(completeDestination);
                }
                return completeDestination;
            }
            catch (Exception ex)
            {
                HandleLoggingOfException(ex);
                throw;
            }
        }

        public string GetFolderOfFile(string filePath)
        {
            if (filePath is null)
                throw new IOException();
            try
            {
                return Path.GetDirectoryName(filePath);

            }
            catch (Exception ex)
            {
                HandleLoggingOfException(ex);
                throw;
            }
        }

        public void DeleteFolderWithContent(string m_DestinationDirectory)
        {
            if (m_DestinationDirectory is null)
                throw new IOException();
            try
            {
                Directory.Delete(m_DestinationDirectory, true);
            }
            catch (Exception ex)
            {
                HandleLoggingOfException(ex);
                throw;
            }

        }

        public void DeleteFolderWithContentIfExist(string m_DestinationDirectory)
        {
            if (Directory.Exists(m_DestinationDirectory))
            {
                DeleteFolderWithContent(m_DestinationDirectory);
            }
        }

        private bool HandleLoggingOfException(Exception ex)
        {
            if (ex is IOException)
                m_Logger.Error(string.Format(Resources.IOEXCEPTION, ex, 0));
            else
                if (ex is DirectoryNotFoundException)
                m_Logger.Error(string.Format(Resources.DIRECTORY_NOT_FOUND, ex, 0));
            else
                m_Logger.Error(string.Format(Resources.ERROR, ex, 0));

            Console.WriteLine(ex.ToString());
            return false;
        }
    }
}