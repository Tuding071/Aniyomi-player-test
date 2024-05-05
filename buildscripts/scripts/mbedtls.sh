#!/bin/bash -e

. ../../include/path.sh

if [ "$1" == "build" ]; then
	true
elif [ "$1" == "clean" ]; then
	make clean
	exit 0
else
	exit 255
fi

$0 clean # separate building not supported, always clean
if [[ "$ndk_triple" == "i686"* ]]; then
	./scripts/config.py unset MBEDTLS_AESNI_C
else
	./scripts/config.py set MBEDTLS_AESNI_C
fi

# enable TLS 1.3
./scripts/config.py set MBEDTLS_SSL_PROTO_TLS1_3
./scripts/config.py set MBEDTLS_PSA_CRYPTO_C
./scripts/config.py set MBEDTLS_SSL_KEEP_PEER_CERTIFICATE

make -j$cores no_test
make DESTDIR="$prefix_dir" install
