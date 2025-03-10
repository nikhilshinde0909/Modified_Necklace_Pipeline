/***********************************************************
 ** Author: Nikhil Shinde <sd1172@srmist.edu.in>
 ** Last Update: 2021/08/07
 *********************************************************/

VERSION="1.11"

//option strings to pass to tools

blat_options="-minScore=200 -minIdentity=98"
blat_related_options="-t=dnax -q=prot -minScore=200 -maxIntron=0"
featurecount_gene_options="--primary -p"
featurecount_block_options="--primary -p --fraction -O"
scaffold_genes=false
de_novo_assembly_file=""

fastqFormatPaired="%_*.gz"
fastqFormatSingle="%.gz"

load args[0]

fastqInputFormat=fastqFormatPaired
if(reads_R2=="") fastqInputFormat=fastqFormatSingle

codeBase = file(bpipe.Config.config.script).parentFile.absolutePath
load codeBase+"/tools.groovy"

load codeBase+"/bpipe_stages/cluster.groovy"
load codeBase+"/bpipe_stages/run_lace.groovy"
load codeBase+"/bpipe_stages/map_reads.groovy"
load codeBase+"/bpipe_stages/get_counts.groovy"
load codeBase+"/bpipe_stages/get_stats.groovy"


/******************* Here are the pipeline stages **********************/

set_input = {
   def files=reads_R1.split(",")
   if(reads_R2!="") files+=reads_R2.split(",")
   forward files
}

run_check = {
    doc "check that the data files exist"
    produce("checks_passed") {
        exec """
            echo "Running necklace version $VERSION" ;
	    echo "Using ${bpipe.Config.config.maxThreads} threads" ;
            echo "Checking for the data files..." ;
	    for i in $genome $annotation $proteins_related_species $inputs.gz ; 
                 do ls $i 2>/dev/null || { echo "CAN'T FIND ${i}..." ;
		 echo "PLEASE FIX PATH... STOPPING NOW" ; exit 1  ; } ; 
	    done ;
            echo "All looking good" ;
            echo "running  necklace version $VERSION.. checks passed" > $output
        ""","checks"
    }
}

nthreads=bpipe.Config.config.maxThreads

run {set_input + run_check + 
     cluster_files +
    run_lace.using(threads: nthreads) + 
    map_reads + //.using(threads: nthreads) + 
    get_counts+ //.using(threads: nthreads) + //
    get_stats } //single thread }
