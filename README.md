# Graphical Editor
## Avery Widen (aewiden)

# Introduction
We're going to build a collaborative graphical editor — akin to Google Docs' ability to have multiple simultaneous editors of the same document. In both cases, multiple clients connect to a server, and whatever editing any of them does, the others see. So I can add an ellipse, a friend on another computer can recolor and move it, etc. In addition to being collaborative, the graphical editor is more functional than the short assignment version (and for extra credit, you can flesh it out even more), in that it handles multiple objects at a time, and can draw rectangles, line segments, and "freehand" shapes in addition to ellipses.

The basic client/server set-up is much like that of the chat server. Each client editor has a thread for talking to the sketch server, along with a main thread for user interaction (previously, getting console input; now, handling the drawing). The server has a main thread to get the incoming requests to join the shared sketch, along with separate threads for communicating with the clients. The client tells the server about its user's drawing actions. The server then tells all the clients about all the drawing actions of each of them.

# Implementation Notes
A scaffold, editor.zip, is provided. It includes a number of files:

Editor: client, handling GUI-based drawing interaction (similar structure to the short assignment)
EditorCommunicator: for messages to/from the server
SketchServer: central keeper of the master sketch; synchronizing the various Editors
SketchServerCommunicator: for messages to/from a single editor (one for each such client)
EchoServer: an alternative server useful for development / debugging
Shape: interface for a graphical shape (with color), with implementations Ellipse, Polyline, Rectangle, and Segment
Please take the time to familiarize yourself with these files, and read over the whole assignment carefully regarding how the pieces fit together, before even thinking about coding.

Since this code builds on the short assignment, the sample solution to that will be posted on Canvas. If you choose to look at it, then of course you are not permitted to revise your own for resubmission for a possible improved score.

Add your code to the scaffold, and provide additional classes and methods to finish it off. You can change type signatures for some of the "your code here" methods if you want, and feel free to add more; it's just to give the structure. The code isn't particularly complicated, but there are a lot of moving parts, and you need to be clear on the protocol (request a sketch update to the server and act on a command from the server).

You will need to develop your own classes to maintain sketches (the shapes shared among the editors) and to handle messages between the editors and server.

## Editor
- The structure of the Editor looks just like that in the short assignment, but now needs to handle multiple shapes, and make and handle requests via the EditorCommunicator instead of directly manipulating a shape.
- Creating a new Rectangle or Segment works much like Ellipse: click one corner and drag to the other. On the other hand, Freehand keeps track of all the points over the course of dragging, allowing an arbitrary curve to be sketched.
- Since the drawing behaviors are quite different for Ellipse vs. Freehand I couldn't come up with a good, clean object-oriented way to handle the current object as just an arbitrary Shape, but instead specifically handled different classes differently. I'd be interested to see a nice (and not stilted) OO approach.
- For simplicity, if you'd like, the process of drawing a shape can be contained just within the editor — when it's complete (button released), tell the server to add it. All the other commands (delete, recolor, move) should be handled as requests to the server.
NOTE: to run multiple Editors in IntelliJ you must click Run->Edit configurations.., then select Editor in the left pane and check "Allow parallel run" in the upper right

## Sketch
- In order to hold the current shape list (either locally for an editor, or globally for the server), I recommend developing an extra class, which for the rest of this write-up I will call "Sketch". Up to you how to implement it.
- All editors need to display and refer to the shapes the same way. Thus they have to be referred to by some global "id", with the sketch maintaining the mapping between ids and shapes. One way to ensure consistency of id numbers:
- The server maintains a "master" version of the sketch, and broadcasts messages to the clients so that their "local" versions are all equivalent.
- To add a new shape (once finished), an editor client sends a request to the server to add it, giving the info needed for a new instance. The server will then determine a unique id for that shape, and broadcast to everyone an indication to add the shape (again, with all the info needed for the constructor) with the given id. Thus all editors have the same id for the same shape.
- Delete, recolor, and move operations use the determined id number.
- The editor and sketch will need to work together to see which shape contains the mouse press, as well as to draw the shapes. Be careful with front-to-back order — look for the topmost containing shape, but draw the bottommost shapes first so that the later ones cover them. If you use a TreeMap for the id-to-shape mapping, with an increasing id number for newer shapes, then a traversal of the tree will yield them in order of newness. That's what TreeMap.descendingKeySet and TreeMap.navigableKeySet do (high-to-low and low-to-high, respectively).
- Because multiple threads will be accessing a sketch (i.e., the different server communicators for the different client editors), the methods that access its shape list will need to be "synchronized".
- Ellipse and Segment are finished; Rectangle should be straightforward if you understand Ellipse.
- A Polyline is like a multi-joint Segment — (x1,y1) to (x2,y2) to (x3,y3), .... It's a geometric representation that can hold a freehand drawing (among other inputs). To test containment of a mouse press in a polyline, you can leverage Segment.pointToSegmentDistance, since each pair of points along a polyline is basically a segment. Note that this method is public static; i.e., you don't have to have a Segment instance.

## Messages
- As described, actions are sent to the server and then back to the client and the other clients.
- Communication is always via Strings, using PrintWriter.println on one side to communicate with BufferedReader.readLine on the other side. You might want to make use of the toString methods of the shapes.
- In order to handle a message, it might be useful to define an extra class that takes a String-based command (sent via writer to reader) and parses it to determine what action to perform and update the sketch accordingly. I recommend just using a simple format for the messages (i.e., no JSON or XML!), with the various pieces separated by spaces or commas. Then, for example, you can unpack the pieces into an array by String.split, and access the pieces by indexing into the array. You can also convert a String to an int via Integer.parseInt.
- When comparing strings (e.g., to decide which action to perform for a message), be sure that you test equality with the method equals, not with ==. For example, tokens[0].equals("a") would return true, but unfortunately tokens[0]=="a" would not.
- Color.toString is nice for reading but not for parsing. On the other hand, its RGB value (flashback to PS-1!), is an int.

## Client-server
- The communicators work much like in the chat server — sending and receiving messages on behalf of the client and server, updating the sketch, and, on the GUI side, having the editor update the display (i.e., don't forget to repaint as necessary).
- When a new client joins, they need to be filled in on the state of the world — what shapes are already there.
- To help in development, we have provided an EchoServer which you can run in place of a SketchServer, testing your Editor and EditorCommunicator before worrying about the server side of things and how to go about handling multiple editors. When the EchoServer is running, it will read in input that your EditorCommunicator sends and "echo" it back so that your
- EditorCommunicator can read it in. This should help you while developing/testing your message protocol for updating a sketch, and that you are actually updating your local sketch correctly according to received messages.

Credit to Dartmouth CS10.
