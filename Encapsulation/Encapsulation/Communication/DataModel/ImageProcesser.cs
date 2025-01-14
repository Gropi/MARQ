using ImageMagick;
using System;
using System.Collections.Generic;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Encapsulation.Communication.DataModel
{
    internal class ImageProcesser
    {
       public string cutBase64Image(string stringImage, int[] bbox)
        {
            var image = MagickImage.FromBase64(stringImage);

            var geometry = new MagickGeometry();
            geometry.Width = bbox[2] - bbox[0];
            geometry.Height = bbox[3] - bbox[1];
            geometry.X = bbox[0];
            geometry.Y = bbox[1];
            
            image.Crop(geometry);
            image.Format = MagickFormat.Jpeg;
            var cutImage = image.ToBase64();

            return cutImage;
        }

        public string BlurAreas(string stringImage, int[][] bboxes)
        {
            var image = MagickImage.FromBase64(stringImage);

            for (int i = 0; i < bboxes.Length; i++)
            {
                var width = bboxes[i][2] - bboxes[i][0];
                var height = bboxes[i][3] - bboxes[i][1];

                var blurFactor = (int)Math.Ceiling(width * height / 100000d);

                var geometry = new MagickGeometry();
                geometry.Width = width;
                geometry.Height = height;
                geometry.X = bboxes[i][0];
                geometry.Y = bboxes[i][1];
                image.RegionMask(geometry);
                image.Blur(50 * blurFactor, 100*blurFactor);
            }

            image.Format = MagickFormat.Jpg;
            var cutImage = image.ToBase64();

            return cutImage;
        }
    }
}
