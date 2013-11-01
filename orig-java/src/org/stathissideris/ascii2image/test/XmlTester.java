/*
 * DiTAA - Diagrams Through Ascii Art
 *
 * Copyright (C) 2004 Efstathios Sideris
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */
package org.stathissideris.ascii2image.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.stathissideris.ascii2image.core.ConversionOptions;
import org.stathissideris.ascii2image.graphics.Diagram;
import org.stathissideris.ascii2image.graphics.DiagramShape;
import org.stathissideris.ascii2image.graphics.DiagramText;
import org.stathissideris.ascii2image.graphics.GraphicalGrid;
import org.stathissideris.ascii2image.graphics.ShapePoint;
import org.stathissideris.ascii2image.text.TextGrid;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * If ran as a Java application, it produces an HTML report for manual inspection. If ran as a junit test it runs a pixel-by-pixel comparison between the images
 * in the "images-expected" and the images generated by the test.
 *
 * @author Efstathios Sideris
 */
@RunWith(Parameterized.class)
public class XmlTester {

	private static final String expectedDir = "tests/xmls-expected";
	private static final String actualDir = "tests/xmls";

	private File textFile;
	private int index;

	public static void main(String[] args) {
		XmlTester.generateXmls(XmlTester.getFilesToRender(), actualDir);
		System.out.println("Done");
	}

	/*
	@Test
	public void compareXmls() throws FileNotFoundException, IOException {
		ConversionOptions options = new ConversionOptions();
		File actualFile = new File(actualDir + File.separator + textFile.getName() + ".png");
		File expectedFile = new File(expectedDir + File.separator + textFile.getName() + ".png");

		System.out.println(index + ") Rendering " + textFile + " to " + actualFile);

		if (!expectedFile.exists()) {
			System.out.println("Skipping " + textFile + " -- reference image does not exist");
			throw new FileNotFoundException("Reference image " + expectedFile + " does not exist");
		}

		TextGrid grid = new TextGrid();
		grid.loadFrom(textFile.toString());
		Diagram diagram = new Diagram(grid, options);

		RenderedImage image = new BitmapRenderer().renderToImage(diagram, options.renderingOptions);

		File file = new File(actualFile.getAbsolutePath());
		ImageIO.write(image, "png", file);

		//compare images pixel-by-pixel
		BufferedImage actualImage = ImageHandler.instance().loadBufferedImage(actualFile);
		BufferedImage expectedImage = ImageHandler.instance().loadBufferedImage(expectedFile);

		assertTrue("Images are not the same size", actualImage.getWidth() == expectedImage.getWidth()
				&& actualImage.getHeight() == expectedImage.getHeight());

		boolean pixelsEqual = true;
		int x = 0;
		int y = 0;

		OUTER: for (y = 0; y < expectedImage.getHeight(); y++) {
			for (x = 0; x < expectedImage.getWidth(); x++) {
				int expectedPixel = expectedImage.getRGB(x, y);
				int actualPixel = actualImage.getRGB(x, y);
				if (actualPixel != expectedPixel) {
					pixelsEqual = false;
					break OUTER;
				}
			}
		}

		assertTrue("Images for " + textFile.getName() + " are not pixel-identical, first different pixel at: "
				+ x + "," + y, pixelsEqual);
	}
	*/

	public XmlTester(File textFile, int index) {
		this.textFile = textFile;
		this.index = index;
	}

	@Parameters
	public static Collection getTestParameters() {
		return VisualTester.getTestParameters();
	}

	public static List<File> getFilesToRender() {
		return VisualTester.getFilesToRender();
	}

	private static class diagram {
		public GraphicalGrid grid;
		public ArrayList<DiagramShape> shapes;
		public ArrayList<DiagramText> texts;

		public diagram(GraphicalGrid grid, ArrayList<DiagramShape> shapes, ArrayList<DiagramText> texts) {
			this.grid = grid;
			this.shapes = shapes;
			this.texts = texts;
		}
	}

	public static void generateXmls(List<File> textFiles, String destinationDir) {

		ConversionOptions options = new ConversionOptions();

		for (File textFile : textFiles) {
			TextGrid grid = new TextGrid();

			File toFile = new File(destinationDir + File.separator + textFile.getName() + ".xml");

			long a = java.lang.System.nanoTime();
			long b;
			try {
				System.out.println("Rendering " + textFile + " to " + toFile);

				grid.loadFrom(textFile.toString());
				Diagram diagram = new Diagram(grid, options);

				b = java.lang.System.nanoTime();
				java.lang.System.out.println("Done in " + Math.round((b - a) / 10e6) + "msec");

				try {
					XStream xstream = new XStream(new DomDriver("utf-8"));
					xstream.alias("diagram", diagram.class);
					xstream.alias("shape", DiagramShape.class);
					xstream.alias("point", ShapePoint.class);
					xstream.useAttributeFor(ShapePoint.class, "x");
					xstream.useAttributeFor(ShapePoint.class, "y");
					xstream.useAttributeFor(ShapePoint.class, "locked");
					xstream.useAttributeFor(ShapePoint.class, "type");
					xstream.alias("text", DiagramText.class);
					xstream.registerConverter(new XmlColorConverter());
					xstream.toXML(new diagram(diagram.getGraphicalGrid(), diagram
							.getAllDiagramShapes(), diagram.getTextObjects()),
							new FileOutputStream(toFile.getAbsolutePath()));
				} catch (IOException e) {
					//e.printStackTrace();
					System.err.println("Error: Cannot write to file " + toFile);
					System.exit(1);
				}

			} catch (Exception e) {
				System.err.println("!!! Failed to render: " + textFile + " !!!\n");
				System.err.println(grid.getDebugString() + "\n");
				e.printStackTrace(System.err);

				continue;
			}
		}

	}
}