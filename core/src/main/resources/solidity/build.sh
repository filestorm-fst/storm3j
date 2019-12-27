#!/usr/bin/env bash

set -e
set -o pipefail

targets="
ens/ENS
ens/PublicResolver
"

for target in ${targets}; do
    dirName=$(dirname $target)
    fileName=$(basename $target)

    cd $dirName
    echo "Compiling Solidity file ${fileName}.sol:"
    solc --bin --abi --optimize --overwrite ${fileName}.sol -o build/
    echo "Complete"

    echo "Generating storm3j bindings"
    storm3j solidity generate \
        -b build/${fileName}.bin \
        -a build/${fileName}.abi \
        -p org.storm3j.ens.contracts.generated \
        -o ../../../../main/java/ > /dev/null
    echo "Complete"

    cd -
done
