#!/usr/bin/env bash

targets="
arrays/build/Arrays
contracts/build/HumanStandardToken
fibonacci/build/Fibonacci
greeter/build/Greeter
shipit/build/ShipIt
simplestorage/build/SimpleStorage
"

for target in ${targets}; do

    storm3j solidity generate \
        -b ../../codegen/src/test/resources/solidity/${target}.bin \
        -a ../../codegen/src/test/resources/solidity/${target}.abi \
        -o /Users/Conor/code/java/storm3j/integration-tests/src/test/java \
        -p org.storm3j.generated

done