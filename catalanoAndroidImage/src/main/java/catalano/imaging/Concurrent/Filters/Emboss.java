// Catalano Android Imaging Library
// The Catalano Framework
//
// Copyright © Diego Catalano, 2012-2016
// diego.catalano at live.com
//
//    This library is free software; you can redistribute it and/or
//    modify it under the terms of the GNU Lesser General Public
//    License as published by the Free Software Foundation; either
//    version 2.1 of the License, or (at your option) any later version.
//
//    This library is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//    Lesser General Public License for more details.
//
//    You should have received a copy of the GNU Lesser General Public
//    License along with this library; if not, write to the Free Software
//    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
//

package catalano.imaging.Concurrent.Filters;

import catalano.imaging.FastBitmap;
import catalano.imaging.IApplyInPlace;

/**
 * Emboss filter.
 * <br />The filter accepts 8 bpp grayscale and 24 bpp color images for processing.
 * 
 * @author Diego Catalano
 */
public class Emboss implements IApplyInPlace{
    
    //Blur Kernel
    int[][] kernel = {
        {-2, 0, 0},
        {0, 1, 0},
        {0, 0, 2}};

    /**
     * Initialize a new instance of the Emboss class.
     */
    public Emboss() {}
    
    /**
     * Apply filter to a FastBitmap.
     */
    @Override
    public void applyInPlace(FastBitmap fastBitmap){
        Convolution c = new Convolution(kernel);
        c.applyInPlace(fastBitmap);
    }
}
