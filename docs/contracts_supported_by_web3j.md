#Contracts supported by storm3j

EIP20
=====

ERC20 tokens are supported via ERC20 contract wrapper as defined in [EIP20](https://github.com/ethereum/EIPs/blob/master/EIPS/eip-20.md) To fetch your token balance you can simply do:

```java
ERC20 contract = ERC20.load(tokenAddress, storm3j, txManager, gasPriceProvider);
BigInteger balance = contract.balanceOf(account).send();
```

EIP165
======

Smart contract interfaces support and discovery as defined in [EIP165](https://github.com/ethereum/EIPs/blob/master/EIPS/eip-165.md) To check whether token contract supports particular interface:

```java
ERC165 contract = ERC165.load(tokenAddress, storm3j, txManager, gasPriceProvider);
Boolean isSupported = contract.supportsInterface(interfaceID).send();
```

EIP721
======

Support for non-fungible tokens, also known as deeds as defined in [EIP721](https://github.com/ethereum/EIPs/blob/master/EIPS/eip-721.md). This contains the following contract wrappers:

-   ERC721 is a set of methods that NFT should support
-   ERC721Metadata optional metadata extension for NFT
-   ERC721Enumerable optional enumeration extension for NFT
