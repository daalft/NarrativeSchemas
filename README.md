# NarrativeSchemas

## About Narrative Schemas

Narrative Schemas are structures extracted from text. This algorithm (originally by Chambers and Jurafsky, 2008/2009) 
automatically extracts information about _events_ from a corpus. _Events_ are verbs and their _dependencies_.
For example, in the text **My dog eats potatoes**, the _events_ would be **eat(subject, dog)** and
**eat(object, potato)**.
From all extracted events, the algorithm then tries to build _schemas_. A schema is a list of _events_
that are likely to occur together in a text. 

## Running the program

* Please note that in order to run this program, you need Java version 1.7. 
* Please note that in order to run this program, you need the Stanford CoreNLP package as well as Apache Commons CLI. 

Stanford CoreNLP can be downloaded at http://nlp.stanford.edu/software/corenlp.shtml  
Apache Commons CLI can be downloaded at http://commons.apache.org/proper/commons-cli/

Unzip both packages.

Compile the java files to a jar file. (In this example, the file is named "NarrativeSchemas.jar")

Set the classpath to include the following files:  
NarrativeSchemas.jar  
commons-cli-1.2.jar  
joda-time.jar  
stanford-corenlp-YYYY-MM-DD-models.jar  
stanford-corenlp-YYYY-MM-DD.jar  
xom.jar  

where YYYY-MM-DD represents a date. The program was written and tested with stanford-corenlp-2012-07-09.
The second file is from the Apache Commons CLI package, the last four files are located in the Stanford CoreNLP package.

Run `starter.TempStart <arguments>`
where `arguments` are as follows:

-buffer path to buffer file. Default: ./buffer  
-error path to error buffer file. Default: ./bufferError  
-corpus path to corpus  
-f flag to indicate that path p is a folder, but does ONLY contain PLAIN TEXT files  
-nyt flag to indicate that path p is a folder containing NYT formatted files  
-output desired filename of final output file  
-size size of schema (number of verbs). Default: 6  
-shuffle shuffle verbs prior to schema building  
-sort sort verbs prior to schema building  
-write write frequency file. Default: false  
-beta set beta value. Default beta value: 0.2  
-lambda set lambda value. Default lambda value: 0.08  
-fpi use full prepositional information for prepositions. Default: false  
-co chain builder only. Only runs the ChainBuilder (first part of algorithm)  
-so schema builder only. Only runs the SchemaBuilder (third part of algorithm)  
-np no parse. Only runs ChainBuilder and SchemaBuilder  

-f and -nyt cannot be set simultaneously.  
-sort and -shuffe can be set simultaneously, but -sort always takes precedence over -shuffle.  
buffer, error, size, shuffle, sort, write, beta, lambda, fpi, co, so and np are optional.

A sample run (assuming that all relevant files are in a folder called "bin") would look like this:

`java -cp bin/commons-cli-1.2.jar;bin/NarrativeSchemas.jar;bin/joda-time.jar;bin/stanford-corenlp-2012-07-06-models.jar;`
`bin/stanford-corenlp-2012-07-09.jar;bin/xom.jar starter.Starter -buffer ./buffer -error ./errorb (-nyt|-f)` 
`-corpus c:/users/david.stephan-pc/downloads/nyt [-np|-co|-so] -output ./schemas_size6_v1 [-size 6] [-shuffle|-sort]` 
`[-write] [-beta 0.3] [-lambda 0.07] [-fpi]`

## Problems

If you notice a lot of skipped files (Skipping file...filename), try running the program with more memory, using -Xmx.
1 GB should suffice. E.g. run:

java -Xmx1024m -cp <classpath-instructions> starter.TempStarter <arguments>

## Interrupting the program

Once running, the program should not be interrupted. If it must be interrupted, it is best to do so while the following is shown
in the console:  
Opening file...filename   
Adding annotator tokenize  
Adding annotator ...  

This way, information from all files prior to the one interrupted will be saved.
The information saved this way is only an intermediate stage of processing.

You can bypass the parsing phase and directly jump to the pair generation and schema builder if you already have a file containing data from a prior interrupted run.
To do this, specify the flag "-np". All other arguments have to be set as well. Please note that the path to the buffer is the file
with the data from a previous run.
