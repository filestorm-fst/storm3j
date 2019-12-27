#!/usr/bin/env bash

targets="
arrays/Arrays
contracts/HumanStandardToken
fibonacci/Fibonacci
greeter/Greeter
misc/Misc
shipit/ShipIt
simplestorage/SimpleStorage
"

for target in ${targets}; do
    dirName=$(dirname $target)
    fileName=$(basename $target)

    cd $dirName
    echo "Compiling Solidity files in ${dirName}:"
    solc --bin --abi --optimize --overwrite ${fileName}.sol -o build/
    echo "Complete"

    echo "Generating storm3j bindings"
    storm3j solidity generate \
        -b build/${fileName}.bin \
        -a build/${fileName}.abi \
        -p org.storm3j.generated \
        -o ../../../../../../integration-tests/src/test/java/ > /dev/null
    echo "Complete"

    cd -
done
