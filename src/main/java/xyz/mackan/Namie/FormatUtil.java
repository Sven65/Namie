// Taken from https://github.com/EssentialsX/Essentials/blob/2.x/Essentials/src/com/earth2me/essentials/utils/FormatUtil.java
// Which is licensed under GPLv3
// With modifications made by me.


package xyz.mackan.Namie;


import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.awt.Color;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FormatUtil {
	private static final Set<ChatColor> COLORS = EnumSet.of(ChatColor.BLACK, ChatColor.DARK_BLUE, ChatColor.DARK_GREEN, ChatColor.DARK_AQUA, ChatColor.DARK_RED, ChatColor.DARK_PURPLE, ChatColor.GOLD, ChatColor.GRAY, ChatColor.DARK_GRAY, ChatColor.BLUE, ChatColor.GREEN, ChatColor.AQUA, ChatColor.RED, ChatColor.LIGHT_PURPLE, ChatColor.YELLOW, ChatColor.WHITE);
	private static final Set<ChatColor> FORMATS = EnumSet.of(ChatColor.BOLD, ChatColor.STRIKETHROUGH, ChatColor.UNDERLINE, ChatColor.ITALIC, ChatColor.RESET);
	private static final Set<ChatColor> MAGIC = EnumSet.of(ChatColor.MAGIC);

	//Vanilla patterns used to strip existing formats
	private static final Pattern STRIP_ALL_PATTERN = Pattern.compile("\u00a7+([0-9a-fk-orA-FK-OR])");
	//Essentials '&' convention colour codes
	private static final Pattern REPLACE_ALL_PATTERN = Pattern.compile("(&)?&([0-9a-fk-orA-FK-OR])");

	private static final Pattern REPLACE_ALL_RGB_PATTERN = Pattern.compile("(&)?&#([0-9a-fA-F]{6})");

	//This method is used to simply strip the native minecraft colour codes
	public static String stripFormat(final String input) {
		if (input == null) {
			return null;
		}
		return ChatColor.stripColor(input);
	}

	//This method is used to simply strip the & convention colour codes
	public static String stripEssentialsFormat(final String input) {
		if (input == null) {
			return null;
		}
		return stripColor(input, REPLACE_ALL_PATTERN);
	}

	//This is the general permission sensitive message format function, checks for urls.
	public static String formatMessage(final Player user, final String input) {
		if (input == null) {
			return null;
		}
		String message = formatString(user, input);
		return message;
	}

	//This method is used to simply replace the ess colour codes with minecraft ones, ie &c
	public static String replaceFormat(final String input) {
		if (input == null) {
			return null;
		}
		return replaceColor(input, EnumSet.allOf(ChatColor.class), true);
	}

	static String replaceColor(final String input, final Set<ChatColor> supported, boolean rgb) {
		StringBuffer legacyBuilder = new StringBuffer();
		Matcher legacyMatcher = REPLACE_ALL_PATTERN.matcher(input);
		legacyLoop: while (legacyMatcher.find()) {
			boolean isEscaped = (legacyMatcher.group(1) != null);
			if (!isEscaped) {
				char code = legacyMatcher.group(2).toLowerCase(Locale.ROOT).charAt(0);
				for (ChatColor color : supported) {
					if (color.getChar() == code) {
						legacyMatcher.appendReplacement(legacyBuilder, "\u00a7$2");
						continue legacyLoop;
					}
				}
			}
			// Don't change & to section sign (or replace two &'s with one)
			legacyMatcher.appendReplacement(legacyBuilder, "&$2");
		}
		legacyMatcher.appendTail(legacyBuilder);

		if (rgb) {
			StringBuffer rgbBuilder = new StringBuffer();
			Matcher rgbMatcher = REPLACE_ALL_RGB_PATTERN.matcher(legacyBuilder.toString());
			while (rgbMatcher.find()) {
				boolean isEscaped = (rgbMatcher.group(1) != null);
				if (!isEscaped) {
					try {
						String hexCode = rgbMatcher.group(2);
						rgbMatcher.appendReplacement(rgbBuilder, parseHexColor(hexCode));
						continue;
					} catch (NumberFormatException ignored) {
					}
				}
				rgbMatcher.appendReplacement(rgbBuilder, "&#$2");
			}
			rgbMatcher.appendTail(rgbBuilder);
			return rgbBuilder.toString();
		}
		return legacyBuilder.toString();
	}

	/**
	 * @throws NumberFormatException If the provided hex color code is invalid or if version is lower than 1.16.
	 */
	public static String parseHexColor(String hexColor) throws NumberFormatException {
		if (hexColor.startsWith("#")) {
			hexColor = hexColor.substring(1); //fuck you im reassigning this.
		}
		if (hexColor.length() != 6) {
			throw new NumberFormatException("Invalid hex length");
		}
		Color.decode("#" + hexColor);
		StringBuilder assembledColorCode = new StringBuilder();
		assembledColorCode.append("\u00a7x");
		for (char curChar : hexColor.toCharArray()) {
			assembledColorCode.append("\u00a7").append(curChar);
		}
		return assembledColorCode.toString();
	}

	static String stripColor(final String input, final Set<ChatColor> strip) {
		StringBuffer builder = new StringBuffer();
		Matcher matcher = STRIP_ALL_PATTERN.matcher(input);
		searchLoop: while (matcher.find()) {
			char code = matcher.group(1).toLowerCase(Locale.ROOT).charAt(0);
			for (ChatColor color : strip) {
				if (color.getChar() == code) {
					matcher.appendReplacement(builder, "");
					continue searchLoop;
				}
			}
			// Don't replace
			matcher.appendReplacement(builder, "$0");
		}
		matcher.appendTail(builder);
		return builder.toString();
	}

	//This is the general permission sensitive message format function, does not touch urls.
	public static String formatString(final Player user, String message) {
		if (message == null) {
			return null;
		}
		EnumSet<ChatColor> supported = EnumSet.noneOf(ChatColor.class);

		if (user.hasPermission("namie.format.color")) {
			supported.addAll(COLORS);
		}
		if (user.hasPermission("namie.format.general")) {
			supported.addAll(FORMATS);
		}
		if (user.hasPermission("namie.format.magic")) {
			supported.addAll(MAGIC);
		}
		for (ChatColor chatColor : ChatColor.values()) {
			String colorName = chatColor.name();
			if (chatColor == ChatColor.MAGIC) {
				// Bukkit's name doesn't match with vanilla's
				colorName = "obfuscated";
			}

			final String node = "namie.format." + colorName.toLowerCase(Locale.ROOT);
			// Only handle individual colors that are explicitly added or removed.
			if (!user.isPermissionSet(node)) {
				continue;
			}
			if (user.hasPermission("namie.format."+node)) {
				supported.add(chatColor);
			} else {
				supported.remove(chatColor);
			}
		}
		EnumSet<ChatColor> strip = EnumSet.complementOf(supported);

		boolean rgb = user.hasPermission("namie.format.rgb");
		if (!supported.isEmpty() || rgb) {
			message = replaceColor(message, supported, rgb);
		}
		if (!strip.isEmpty()) {
			message = stripColor(message, strip);
		}
		return message;
	}

	static String stripColor(final String input, final Pattern pattern) {
		return pattern.matcher(input).replaceAll("");
	}

	public static String lastCode(final String input) {
		int pos = input.lastIndexOf('\u00a7');
		if (pos == -1 || (pos + 1) == input.length()) {
			return "";
		}
		return input.substring(pos, pos + 2);
	}
}