package Quadtree;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Scanner;

public class QuadTreeManager {

	String db;
	Connection con;
	Statement stmt;
	String textFile;

	public QuadTreeManager(String db, String textFile) {
		this.textFile = textFile;
		this.db = db;
		this.con = getConnection();
		try {
			this.stmt = con.createStatement();
			createTables(stmt, con);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public Connection getConnection() {
		Connection conn = null;
		try {

			String url = "jdbc:sqlite:test.db";
			conn = DriverManager.getConnection(url);
			System.out.println("CONNECTION SUCCESSFULL FOR SQLITE");

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return conn;
	}

	public void createTables(Statement stmt, Connection con) {
		String drop = "DROP TABLE QUAD_TREE";
		String drop1 = "DROP TABLE QUAD_TREE_RECT";
		String drop2 = "DROP TABLE QUAD_TREE_POINT";
		try {
			stmt.execute(drop);
			stmt.execute(drop1);
			stmt.execute(drop2);
			System.out.println(":: TABLES DELETE SUCCESS");
			createTables(stmt, con);
		} catch (SQLException s) {
			String quad_tree = "CREATE TABLE QUAD_TREE (FILE_NAME VARCHAR(64), ROOT_RECT_ID INTEGER)";

			String quad_tree_rect = "CREATE TABLE QUAD_TREE_RECT "
					+ "(ID INTEGER PRIMARY KEY AUTOINCREMENT, PARENT_ID INTEGER, X_LOW REAL, Y_LOW REAL, "
					+ "X_HIGH REAL, Y_HIGH REAL)";

			String quad_tree_point = "CREATE TABLE QUAD_TREE_POINT (QUAD_RECT_ID INTEGER, X REAL, Y REAL, LABEL CHAR(16))";

			try {
				stmt.execute(quad_tree);
				stmt.execute(quad_tree_rect);
				stmt.execute(quad_tree_point);
				System.out.println(":: TABLES CREATE SUCCESS");
			}

			catch (Exception e) {
				e.printStackTrace();
				s.printStackTrace();
			}
		}
	}

	public boolean createQuadTree(String fileName, float lowX, float lowY, float highX, float highY) {
		String insert_rect = " INSERT INTO QUAD_TREE_RECT (PARENT_ID, X_LOW, Y_LOW, X_HIGH, Y_HIGH)" + " VALUES (-1,"
				+ lowX + "," + lowY + "," + highX + "," + highY + ")";
		String getID = "SELECT MAX(ID) as ID FROM QUAD_TREE_RECT";

		if (lowX > highX || lowY > highY) {
			System.out.println("FAILURE -> LOW IS GREATER THAN HIGH FOR X OR Y.");
			return false;
		} 
		else {
			try {
				stmt.execute(insert_rect);
				ResultSet rs = stmt.executeQuery(getID);
				int ID = rs.getInt("ID");
				
				String insert_tree = "INSERT INTO QUAD_TREE (FILE_NAME, ROOT_RECT_ID) " + 
				"VALUES('" + fileName + "', "+ ID + ")";
				stmt.execute(insert_tree);
				System.out.println("::SUCCESS -> Inserted into QUAD_TREE AND QUAD_TREE_RECT for createQuadTree method");
				return true;
			} catch (SQLException s) {
				s.printStackTrace();
				return false;
			}
		}
	}

	public void readTextFile() {
		File file = new File(textFile);
		try {
			Scanner input = new Scanner(file);
			ArrayList<String> list = new ArrayList<String>();

			while (input.hasNext()) {
				list.add(input.next());
			}

			for (int i = 0; i < list.size(); i++) {
				// c means create new Quadtree
				if (list.get(i).equalsIgnoreCase("c")) {
					String file_name = list.get(i + 1);
					Float low_x = Float.parseFloat(list.get(i + 2));
					Float low_y = Float.parseFloat(list.get(i + 3));
					Float high_x = Float.parseFloat(list.get(i + 4));
					Float high_y = Float.parseFloat(list.get(i + 5));

					// from here need to do the required instructions with the above strings
					// create QuadTree here
					createQuadTree(file_name, low_x, low_y, high_x, high_y);

				}

				if (list.get(i).equalsIgnoreCase("i")) {
					String file_name = list.get(i + 1);
					String label = list.get(i + 2);
					String x_value = list.get(i + 3);
					String y_value = list.get(i + 4);

					float x = Float.parseFloat(x_value);
					float y = Float.parseFloat(y_value);

					QuadRecord record = new QuadRecord(label, new QuadPoint(x, y));
					add(file_name,record);

				}

				if (list.get(i).equalsIgnoreCase("l")) {
					String file_name = list.get(i + 1);
					Float p1_x = Float.parseFloat(list.get(i + 2));
					Float p1_y = Float.parseFloat(list.get(i + 3));
					Float p2_x = Float.parseFloat(list.get(i + 4));
					Float p2_y = Float.parseFloat(list.get(i + 5));
					int limit_offset = Integer.parseInt(list.get(i+6));
					int limit_count = Integer.parseInt(list.get(i+7));
					
					QuadRectangle rec = new QuadRectangle(100, new QuadPoint(p1_x, p1_y), new QuadPoint(p2_x,p2_y));
					QuadRectangle[] recs = lookupRectangle(file_name, rec);
					if(recs.length == 10) {
						System.out.println("No intersecting rectangles.");
					}

					// lookup stuff from here
					QuadRecord[] points = lookupPoint(file_name, new QuadPoint(p1_x, p1_y), new QuadPoint(p2_x,p2_y), limit_offset, limit_count);
				}

			}
			System.out.println("Done reading file.");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/*
	 * STEP 1: This function should insert the passed QuadRecord into the quad tree
	 * given by fileName. To do this it should look up in QUAD_TREE to see if such a
	 * quad tree exists.
	 * 
	 * STEP 2: If it does, it should get the QUAD_TREE_RECT row corresponding to
	 * ROOT_RECT_ID, and see if the record has a point that can be inserted into
	 * this quad tree.
	 * 
	 * STEP 3; If it does it should do a query for QUAD_TREE_RECT rows with
	 * PARENT_ID having value ROOT_RECT_ID, and so on to trace down through the quad
	 * tree to where the record should be inserted.
	 * 
	 * If a quad needs to be split (is a leaf witth more than 4 records) the
	 * appropriate QUARD_TREE_RECT rows should be added to the table and existing
	 * QUAD_TREE_POINT rows in the now subdivided quad should have their
	 * QUAD_RECT_ID's updated.
	 * 
	 * STEP 4: Finally, an appropriate new QUAD_TREE_POINT record should be added
	 * for record.
	 */
	public boolean add(String fileName, QuadRecord record) {
		String checkIfExists = "SELECT * from QUAD_TREE WHERE FILE_NAME = '" + fileName + "'";
		boolean worked = false;

		try {

			// STEP 1, get quadTree root ID
			ResultSet rs = stmt.executeQuery(checkIfExists);
			boolean exists = false;
			exists = rs.next();
			System.out.println(
					"\n \n For your insertion of a quadtree record, there is an (or not) existing QuadTree in the table: "
							+ exists);
			int treeID = rs.getInt("ROOT_RECT_ID");

			if (exists == true) {

				// STEP 2
				// get multiple Rectangle rows
				String getRect = "SELECT * FROM QUAD_TREE_RECT WHERE ID = " + treeID;
				ResultSet r = stmt.executeQuery(getRect);

				//checking each rectangle
				while (rs.next()) {
					int rectID = r.getInt("ID");
					float low_x = r.getFloat("X_LOW");
					float low_y = r.getFloat("Y_LOW");
					float high_x = r.getFloat("X_HIGH");
					float high_y = r.getFloat("Y_HIGH");
					
					//check if each rectangle has 4 or more points
					String checkIfFull = "SELECT COUNT(QUAD_RECT_ID) AS recs FROM QUAD_TREE_POINT WHERE QUAD_RECT_ID =" + rectID;
					ResultSet fullPoints = stmt.executeQuery(checkIfFull);
					fullPoints.next();
					int numOfRecords = fullPoints.getInt("recs");
					
					
					
					if (numOfRecords > 3) {
						float midpoint1 = (high_y + low_y)/2;
						float midpoint2 = (high_x + low_x)/2;
			
				
						//treat as parent node from here, we treat this as a parent node so it will need to have 4 child nodes 
						//(which may or may not be a leaf node), we only check when we insert
						
						//Create 4 new quadrants from large rectangle points, then check which in which quadrant to insert
						
						
						System.out.println("There are more than 4 records. Will make this rectangle with ID " + rectID + "as a parent node and add child quadrants");
						System.out.println("\n\n\nCREATING QUADRANTS.... ");
						for(int i = 1; i <= 4; i++) {
							
							if(i == 1) {
								
								//Create new quadrant
								String quad1 = "INSERT INTO QUAD_TREE_RECT (PARENT_ID, X_LOW, Y_LOW, X_HIGH, Y_HIGH) "
										+ "VALUES (" + rectID + "," +  low_x + "," + midpoint1 + "," + midpoint2 + "," + high_y + ")";
								
								try {
									stmt.execute(quad1);
									
									ResultSet quad1ID = stmt.executeQuery("SELECT Max(ID) AS ID FROM QUAD_TREE_RECT");
									quad1ID.next();
									
									//get ID for new rectangle in case we need to insert
									int newID = quad1ID.getInt("ID");
									System.out.println("\n Quadrant1 Rectangle has been created. ID is" + newID);
									
									//check if point fits
									boolean check = checkIfPoint(low_x, midpoint1, midpoint2, high_y, record);
									System.out.println("Point does or does not fit in the rectangle: " + check);
									
									if(check == true) {
										try {
											String insert = "INSERT INTO QUAD_TREE_POINT (QUAD_RECT_ID, X, Y, LABEL) " + "VALUES ("
													+ newID + "," + record.point.x + "," + record.point.y + ", '" + record.getLabel()
													+ "')";
											stmt.execute(insert);
											System.out.println("SUCCESS-> Record was inserted at rectangle ID " + newID);
											QuadRectangle newRec = new QuadRectangle(newID, new QuadPoint(low_x, high_y), new QuadPoint(midpoint2, midpoint1));
											updatePoints(rectID, newRec);
											
										} catch (SQLException s) {
											s.printStackTrace();
											System.out.println("COULD NOT INSERT RECORD");
											return false;
										}
									
									}
									
									
								}
								catch(SQLException s) {
									s.printStackTrace();
								}
								
								
								
							}
							
							
							else if(i == 2) {
								//Quad2
								String quad2 = "INSERT INTO QUAD_TREE_RECT (PARENT_ID, X_LOW, Y_LOW, X_HIGH, Y_HIGH) "
										+ "VALUES (" + rectID + "," + midpoint2 + "," + midpoint1 + "," + high_x + "," + high_y + ")";
								try {
									stmt.executeUpdate(quad2);
									ResultSet quad2ID = stmt.executeQuery("SELECT Max(ID) AS ID FROM QUAD_TREE_RECT");
									quad2ID.next();
									
									//get ID for new rectangle in case we need to insert
									int newID = quad2ID.getInt("ID");
									System.out.println(" Quadrant2 Rectangle has been created. ID is " + newID);
									
									//check if point fits
									boolean check = checkIfPoint(midpoint2, midpoint1, high_x, high_y, record);
									
									System.out.println("Point does or does not fit in the rectangle: " + check);
									if(check == true) {
										try {
											String insert = "INSERT INTO QUAD_TREE_POINT (QUAD_RECT_ID, X, Y, LABEL) " + "VALUES ("
													+ newID + "," + record.point.x + "," + record.point.y + ", '" + record.getLabel()
													+ "')";
											stmt.execute(insert);
											System.out.println("SUCCESS-> Record was inserted at rectangle ID " + newID);
											QuadRectangle newRec = new QuadRectangle(newID, new QuadPoint(midpoint2, high_y), new QuadPoint(high_x, midpoint1));
											updatePoints(rectID, newRec);
									
										} catch (SQLException s) {
											s.printStackTrace();
											System.out.println("COULD NOT INSERT RECORD");
											return false;
										}
									}
								}
								catch(SQLException s) {
									s.printStackTrace();
								}
							}

							
							else if(i == 3) {
								//Quad3
								String quad3 = "INSERT INTO QUAD_TREE_RECT (PARENT_ID, X_LOW, Y_LOW, X_HIGH, Y_HIGH) "
										+ "VALUES (" + rectID + "," + low_x + "," + low_y + "," + midpoint2 + "," + midpoint1 + ")";
								
								try {
									stmt.execute(quad3);
									ResultSet quad3ID = stmt.executeQuery("SELECT Max(ID) AS ID FROM QUAD_TREE_RECT");
									quad3ID.next();
									
									//get ID for new rectangle in case we need to insert
									int newID = quad3ID.getInt("ID");
									System.out.println(" Quadrant3 Rectangle has been created. ID is " + newID);
									
									//check if point fits
									boolean check = checkIfPoint(low_x, low_y, midpoint2, midpoint1, record);
									System.out.println("Point does or does not fit in the rectangle: " + check);
									
									
									if(check == true) {
										try {
											String insert = "INSERT INTO QUAD_TREE_POINT (QUAD_RECT_ID, X, Y, LABEL) " + "VALUES ("
													+ newID + "," + record.point.x + "," + record.point.y + ", '" + record.getLabel()
													+ "')";
											stmt.execute(insert);
											System.out.println("SUCCESS-> Record was inserted at rectangle ID " + newID);
											QuadRectangle newRec = new QuadRectangle(newID, new QuadPoint(low_x, midpoint1), new QuadPoint(midpoint2, low_y));
											updatePoints(rectID, newRec);
										} catch (SQLException s) {
											s.printStackTrace();
											System.out.println("COULD NOT INSERT RECORD");
											return false;
										}
									}
								}
								catch(SQLException s) {
									s.printStackTrace();
								}
							}
							
							else {
								
								//Quad 4
								String quad4 = "INSERT INTO QUAD_TREE_RECT (PARENT_ID, X_LOW, Y_LOW, X_HIGH, Y_HIGH) "
										+ "VALUES (" + rectID + "," + midpoint2 + "," + low_y + "," + high_x + "," + midpoint1 + ")";
								try {
									stmt.execute(quad4);
									ResultSet quad4ID = stmt.executeQuery("SELECT Max(ID) AS ID FROM QUAD_TREE_RECT");
									quad4ID.next();
									
									//get ID for new rectangle in case we need to insert
									int newID = quad4ID.getInt("ID");
									System.out.println(" Quadrant3 Rectangle has been created. ID is " + newID);
									//check if point fits
									boolean check = checkIfPoint(midpoint2, low_y, high_x, midpoint1, record);
									System.out.println("Point does or does not fit in the rectangle: " + check);
									
									if(check == true) {
										try {
											String insert = "INSERT INTO QUAD_TREE_POINT (QUAD_RECT_ID, X, Y, LABEL) " + "VALUES ("
													+ newID + "," + record.point.x + "," + record.point.y + ", '" + record.getLabel()
													+ "')";
											stmt.execute(insert);
											System.out.println("SUCCESS-> Record was inserted at rectangle ID " + newID);
											QuadRectangle newRec = new QuadRectangle(newID, new QuadPoint(midpoint2, midpoint1), new QuadPoint(high_x, low_y));
											updatePoints(rectID, newRec);
										
										} catch (SQLException s) {
											s.printStackTrace();
											System.out.println("COULD NOT INSERT RECORD");
											return false;
										}
									}
									
								}
								catch(SQLException s) {
									s.printStackTrace();
								}
							}
							
							
							
							
						}
						
					} else {
						
						//less than 4 points so it is a leaf node
						if (record.point.x > low_x && record.point.x < high_x && record.point.y > low_y
								&& record.point.y < high_y) {

							try {
								String insert = "INSERT INTO QUAD_TREE_POINT (QUAD_RECT_ID, X, Y, LABEL) " + "VALUES ("
										+ rectID + "," + record.point.x + "," + record.point.y + ", '" + record.getLabel()
										+ "')";
								stmt.execute(insert);
								System.out.println("SUCCESS-> Record was inserted at rectangle ID " + rectID);
							} catch (SQLException s) {
								s.printStackTrace();
								System.out.println("COULD NOT INSERT RECORD");
								return false;
							}

						} else {
							System.out.println("Record point does not fit in rectangle");
							return false;
						}
					}
				}
				
			}

			else {
				System.out.println("ERROR. No existence of tree.");
				return false;
			}
		}

		catch (SQLException s) {
			s.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return worked;
	}
	
	public boolean checkIfPoint(float low_x, float low_y, float high_x, float high_y, QuadRecord record) {
		if (record.point.x > low_x && record.point.x < high_x && record.point.y > low_y
				&& record.point.y < high_y) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public void updatePoints(int BigRectID, QuadRectangle quad) {
		String s = "SELECT * FROM QUAD_TREE_POINT WHERE QUAD_RECT_ID = " + BigRectID;
		System.out.println("\n\n\n UPDATING POINTS....");
		try {
			ResultSet allPoints = stmt.executeQuery(s);
			while(allPoints.next()) {
				String label = allPoints.getString("Label");
				
				float x = allPoints.getFloat("X");
				float y = allPoints.getFloat("Y");
				System.out.println("Label: " + label + " x: " + x + " y: " +y);
				System.out.println(quad.print());
				
				if (x >= quad.bottom_left.x && x <= quad.top_right.x && y >= quad.bottom_left.y
						&& y <= quad.top_right.y) {
					String switchRec = "UPDATE QUAD_TREE_POINT SET QUAD_RECT_ID = " + quad.id + " WHERE QUAD_RECT_ID = " + BigRectID;
					stmt.execute(switchRec);
					System.out.println("SUCCESS SWITCHED POINT ID");
					
				}
			
				

			}
		}
		catch(SQLException x) {
			x.printStackTrace();
		}
	}
	
	public float distance(float x1, float y1, float x2, float y2) {
		return (float) Math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1));
	}

	
	
	 public QuadRectangle[] lookupRectangle(String fileName, QuadRectangle r) {
		 
		 
		 System.out.println("\n \n \n \nLOOKING FOR INTERSECTING RECTANGLES...");
	 ArrayList<QuadRectangle> allRecs = new ArrayList<QuadRectangle>(); 
	 String getRects = "SELECT * from QUAD_TREE WHERE FILE_NAME = '" + fileName + "'";
	 try {
	 ResultSet rects = stmt.executeQuery(getRects); 
	 rects.next();
	 int root = rects.getInt("ROOT_RECT_ID");
	 
	 
	 String findRectangles = "SELECT * FROM QUAD_TREE_RECT WHERE PARENT_ID =" + root;
	 ResultSet recs = stmt.executeQuery(findRectangles);
	  
	 while(recs.next()) {
		 int id = recs.getInt("ID");
		float low_x = recs.getFloat("X_LOW");
		float low_y = recs.getFloat("Y_LOW");
		float high_x = recs.getFloat("X_HIGH");
		float high_y = recs.getFloat("Y_HIGH");
		
		QuadRectangle newRec = new QuadRectangle(id, new QuadPoint(low_x, high_y), new QuadPoint(high_x, low_y));
	
		boolean overlaps;
		 if (r.bottom_left.x <= newRec.bottom_right.x && r.bottom_right.x >= newRec.bottom_left.x &&
				    r.top_left.y <= newRec.top_right.y && r.top_right.y <= newRec.top_left.y) {
			        overlaps = true;
			        }
		 else {
			 overlaps = false;
			 }
		 
		 if(overlaps == true) {
			 allRecs.add(newRec);
		 }
			
	 }
	 
	 System.out.println("Query intersects with the following rectangles in the quad tree: ");
	 QuadRectangle[] array = new QuadRectangle[allRecs.size()];
	 for(int i = 0; i < allRecs.size(); i++) {
		 System.out.println(allRecs.get(i).print());
		 array[i] = allRecs.get(i);
	 }
	  
	 System.out.println("The Intersecting Rectangles are above.");
	 return array;
	 } 
	 catch(SQLException s)
	 { s.printStackTrace(); }
	 
	 return new QuadRectangle[10];
	 
	 }
	 
	
	 public QuadRecord[] lookupPoint (String fileName, QuadPoint pt1, QuadPoint
	 pt2, int limit_offset, int limit_count) {
		 QuadRectangle rec = new QuadRectangle(100, pt1, pt2);
		 System.out.println("\n \n \n \nLOOKING FOR POINTS...");
		 
		 System.out.println("Your rectangle from p1 and p2 is: " + rec.print());
		 ArrayList<QuadRecord> allPoints = new ArrayList<QuadRecord>(); 
		 ArrayList<Integer> rectanglesIntheTree = new ArrayList<Integer>();
		 QuadRecord[] specificPoints = new QuadRecord[limit_count];
		 
		 
		 String getRects = "SELECT * from QUAD_TREE WHERE FILE_NAME = '" + fileName + "'";
		 try {
		 ResultSet rects = stmt.executeQuery(getRects); 
		 rects.next();
		 int root = rects.getInt("ROOT_RECT_ID");
		 
		 String findRectangles = "SELECT * FROM QUAD_TREE_RECT \n" + 
		 		"WHERE PARENT_ID = " + root + 
		 		" AND PARENT_ID = (SELECT PARENT_ID FROM QUAD_TREE_RECT WHERE PARENT_ID = " +root + ")";
		 ResultSet recs = stmt.executeQuery(findRectangles);
		 
		 
		 while(recs.next()) {
			 int id = recs.getInt("ID");
			 rectanglesIntheTree.add(id);
		 }
			 
		 for(int i = 0; i < rectanglesIntheTree.size(); i++) {
		 String points = "SELECT * FROM QUAD_TREE_POINT WHERE QUAD_RECT_ID = " + rectanglesIntheTree.get(i);
			 ResultSet allPoint = stmt.executeQuery(points);
			 while(allPoint.next()) {
				 String label = allPoint.getString("Label");
				 Float x = allPoint.getFloat("X");
				 Float y = allPoint.getFloat("Y");
				 allPoints.add(new QuadRecord(label, new QuadPoint(x, y)));
			 }
		 }
			
		 }
		  
		 
		 catch(SQLException s) {
			 s.printStackTrace();
		 }
		 

		 
		 System.out.println("Quad tree records satisfying the query: ");
		 int count = 0;
		 for(int i = 0; i < allPoints.size(); i++) {
			 System.out.println(allPoints.get(i));
			 System.out.println(check(rec, allPoints.get(i)));
			 boolean checkIn = checkIfPoint(rec.bottom_left.x, rec.bottom_left.y, rec.top_right.x, rec.top_right.y, allPoints.get(i));
			 if(checkIn == true) {
				 specificPoints[count] = allPoints.get(i);
				 System.out.println("(" + specificPoints[count].getLabel() + ", " + specificPoints[count].point.x + ", " + specificPoints[count].point.y + ")" );
				 count++;
						
			 }
		 }
		  return specificPoints;
		 
	 }
	
	 public boolean check(QuadRectangle quad, QuadRecord quadr) {
		 if(quad.bottom_left.y < quadr.point.y) {
			 return true;
		 } else return false;
	 }

	public static void main(String args[]) {

		/*
		 * Creating a new QuadTreeManager object will: 1. Store database name and Create
		 * a connection 2. Store the text file url 3. and create new tables
		 */
		QuadTreeManager manager = new QuadTreeManager(args[0], args[1]);

		/*
		 * This will read the text file and then execute instructions
		 */

		manager.readTextFile();
	}
}