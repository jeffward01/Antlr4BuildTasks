/*
 * Copyright (c) 2012 The ANTLR Project. All rights reserved.
 * Use of this file is governed by the BSD-3-Clause license that
 * can be found in the LICENSE.txt file in the project root.
 */
package org.antlr.v4.runtime;

import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.misc.Nullable;

import java.util.Arrays;

/**
 * This class provides a default implementation of the {@link Vocabulary}
 * interface.
 *
 * @author Sam Harwell
 *
 * @sharpen.rename Vocabulary
 */
public class VocabularyImpl implements Vocabulary {
	private static final String[] EMPTY_NAMES = new String[0];

	/**
	 * Gets an empty {@link Vocabulary} instance.
	 *
	 * <p>
	 * No literal or symbol names are assigned to token types, so
	 * {@link #getDisplayName(int)} returns the numeric value for all tokens
	 * except {@link Token#EOF}.</p>
	 */
	@NotNull
	public static final VocabularyImpl EMPTY_VOCABULARY = new VocabularyImpl(EMPTY_NAMES, EMPTY_NAMES, EMPTY_NAMES);

	@NotNull
	private final String[] literalNames;
	@NotNull
	private final String[] symbolicNames;
	@NotNull
	private final String[] displayNames;

	private final int maxTokenType;

	/**
	 * Constructs a new instance of {@link VocabularyImpl} from the specified
	 * literal and symbolic token names.
	 *
	 * @param literalNames The literal names assigned to tokens, or {@code null}
	 * if no literal names are assigned.
	 * @param symbolicNames The symbolic names assigned to tokens, or
	 * {@code null} if no symbolic names are assigned.
	 *
	 * @see #getLiteralName(int)
	 * @see #getSymbolicName(int)
	 */
	public VocabularyImpl(@Nullable String[] literalNames, @Nullable String[] symbolicNames) {
		this(literalNames, symbolicNames, null);
	}

	/**
	 * Constructs a new instance of {@link VocabularyImpl} from the specified
	 * literal, symbolic, and display token names.
	 *
	 * @param literalNames The literal names assigned to tokens, or {@code null}
	 * if no literal names are assigned.
	 * @param symbolicNames The symbolic names assigned to tokens, or
	 * {@code null} if no symbolic names are assigned.
	 * @param displayNames The display names assigned to tokens, or {@code null}
	 * to use the values in {@code literalNames} and {@code symbolicNames} as
	 * the source of display names, as described in
	 * {@link #getDisplayName(int)}.
	 *
	 * @see #getLiteralName(int)
	 * @see #getSymbolicName(int)
	 * @see #getDisplayName(int)
	 */
	public VocabularyImpl(@Nullable String[] literalNames, @Nullable String[] symbolicNames, @Nullable String[] displayNames) {
		this.literalNames = literalNames != null ? literalNames : EMPTY_NAMES;
		this.symbolicNames = symbolicNames != null ? symbolicNames : EMPTY_NAMES;
		this.displayNames = displayNames != null ? displayNames : EMPTY_NAMES;
		// See note here on -1 part: https://github.com/antlr/antlr4/pull/1146
		this.maxTokenType =
			Math.max(this.displayNames.length,
					 Math.max(this.literalNames.length, this.symbolicNames.length)) - 1;
	}

	/**
	 * Returns a {@link VocabularyImpl} instance from the specified set of token
	 * names. This method acts as a compatibility layer for the single
	 * {@code tokenNames} array generated by previous releases of ANTLR.
	 *
	 * <p>The resulting vocabulary instance returns {@code null} for
	 * {@link #getLiteralName(int)} and {@link #getSymbolicName(int)}, and the
	 * value from {@code tokenNames} for the display names.</p>
	 *
	 * @param tokenNames The token names, or {@code null} if no token names are
	 * available.
	 * @return A {@link Vocabulary} instance which uses {@code tokenNames} for
	 * the display names of tokens.
	 */
	public static Vocabulary fromTokenNames(@Nullable String[] tokenNames) {
		if (tokenNames == null || tokenNames.length == 0) {
			return EMPTY_VOCABULARY;
		}

		String[] literalNames = Arrays.copyOf(tokenNames, tokenNames.length);
		String[] symbolicNames = Arrays.copyOf(tokenNames, tokenNames.length);
		for (int i = 0; i < tokenNames.length; i++) {
			String tokenName = tokenNames[i];
			if (tokenName == null) {
				continue;
			}

			if (!tokenName.isEmpty()) {
				char firstChar = tokenName.charAt(0);
				if (firstChar == '\'') {
					symbolicNames[i] = null;
					continue;
				}
				else if (Character.isUpperCase(firstChar)) {
					literalNames[i] = null;
					continue;
				}
			}

			// wasn't a literal or symbolic name
			literalNames[i] = null;
			symbolicNames[i] = null;
		}

		return new VocabularyImpl(literalNames, symbolicNames, tokenNames);
	}

	@Override
	public int getMaxTokenType() {
		return maxTokenType;
	}

	@Override
	@Nullable
	public String getLiteralName(int tokenType) {
		if (tokenType >= 0 && tokenType < literalNames.length) {
			return literalNames[tokenType];
		}

		return null;
	}

	@Override
	@Nullable
	public String getSymbolicName(int tokenType) {
		if (tokenType >= 0 && tokenType < symbolicNames.length) {
			return symbolicNames[tokenType];
		}

		if (tokenType == Token.EOF) {
			return "EOF";
		}

		return null;
	}

	@Override
	@NotNull
	public String getDisplayName(int tokenType) {
		if (tokenType >= 0 && tokenType < displayNames.length) {
			String displayName = displayNames[tokenType];
			if (displayName != null) {
				return displayName;
			}
		}

		String literalName = getLiteralName(tokenType);
		if (literalName != null) {
			return literalName;
		}

		String symbolicName = getSymbolicName(tokenType);
		if (symbolicName != null) {
			return symbolicName;
		}

		return Integer.toString(tokenType);
	}
}
