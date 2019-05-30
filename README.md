# Duke-CS201-HuffmanCodingProject
Project implementing the Huffman Coding Procedure for data compression and decompression.

From the project description:

There are many techniques used to compress digital data. This assignment covers Huffman Coding.

Huffman coding was invented by David Huffman while he was a graduate student at MIT in 1950 when given the option of a term paper or a final exam. In an autobiography Huffman had this to say about the epiphany that led to his invention of the coding method that bears his name:

"-- but a week before the end of the term I seemed to have nothing to show for weeks of effort. I knew I'd better get busy fast, do the standard final, and forget the research problem. I remember, after breakfast that morning, throwing my research notes in the wastebasket. And at that very moment, I had a sense of sudden release, so that I could see a simple pattern in what I had been doing, that I hadn't been able to see at all until then. The result is the thing for which I'm probably best known: the Huffman Coding Procedure. I've had many breakthroughs since then, but never again all at once, like that. It was very exciting."

The Wikipedia reference is extensive as is this online material developed as one of the original Nifty Assignments. Both jpeg and mp3 encodings use Huffman Coding as part of their compression algorithms. In this assignment you'll implement a complete program to compress and uncompress data using Huffman coding.

You should read about the Huffman algorithm, you won't be able to complete project easily without the overview you'll get from this reading: https://www.cs.duke.edu/csed/poop/huff/info/ .

When you've read the description of the algorithm and data structures used you'll be ready to implement both decompression (aka uncompressing) and compression  using Huffman Coding. You'll be using input and output or I/O classes that read and write 1 to many bits at a time, i.e., a single zero or one to several zeros and ones. This will make debugging your program a challenge.
