package com.vault.engine;

import java.nio.charset.StandardCharsets;

/**
 * Deterministic hashing utility used for sticky rollouts.
 *
 * We intentionally avoid randomness so a given (featureKey, tenantId/userId) pair
 * always maps to the same bucket.
 */
public final class MurmurHash3 {
	private MurmurHash3() {}

	// MurmurHash3 x86 32-bit variant (public domain reference algorithm).
	public static int murmur3_32(String input) {
		byte[] data = input.getBytes(StandardCharsets.UTF_8);
		int length = data.length;

		int h1 = 0; // seed = 0 for determinism across services
		final int c1 = 0xcc9e2d51;
		final int c2 = 0x1b873593;

		int roundedEnd = (length & 0xfffffffc); // round down to 4 byte block
		for (int i = 0; i < roundedEnd; i += 4) {
			int k1 = (data[i] & 0xff) | ((data[i + 1] & 0xff) << 8) | ((data[i + 2] & 0xff) << 16)
					| ((data[i + 3] & 0xff) << 24);

			k1 *= c1;
			k1 = Integer.rotateLeft(k1, 15);
			k1 *= c2;

			h1 ^= k1;
			h1 = Integer.rotateLeft(h1, 13);
			h1 = h1 * 5 + 0xe6546b64;
		}

		int k1 = 0;
		int tailStart = roundedEnd;
		switch (length & 0x03) {
			case 3 -> k1 ^= (data[tailStart + 2] & 0xff) << 16;
			case 2 -> k1 ^= (data[tailStart + 1] & 0xff) << 8;
			case 1 -> {
				k1 ^= (data[tailStart] & 0xff);
				k1 *= c1;
				k1 = Integer.rotateLeft(k1, 15);
				k1 *= c2;
				h1 ^= k1;
			}
			default -> {}
		}

		// finalization mix - force all bits of a hash block to avalanche
		h1 ^= length;
		h1 ^= (h1 >>> 16);
		h1 *= 0x85ebca6b;
		h1 ^= (h1 >>> 13);
		h1 *= 0xc2b2ae35;
		h1 ^= (h1 >>> 16);

		return h1;
	}
}

