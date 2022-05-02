package Core.Commands.BDO;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandMessage;
import Core.CommandSystem.SlashMessageBuilder;
import Core.Commands.BDO.BDOItem.BDOItemData;
import Core.Commands.BDO.BDOItem.BDOItemEnhancement;
import Core.Main.Logging;
import Core.Objects.Annotation.Commands.SlashArgument;
import Core.Objects.Annotation.Commands.SubCommand;
import Core.Objects.BotChannel;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.List;
import java.util.*;

@SubCommand( parent = BDOSlashCommand.class)
public class BDOItemEnhanceCommand extends BDOItemCommand implements ISlashCommand
{
	@SlashArgument( key = "enhancement", text = "Which enhancement level to show.", required = true, choices = {"PEN", "TET", "TRI", "DUO", "PRI",
	                                                                                           "+15", "+14", "+13", "+12", "+11",
	                                                                                           "+10", "+9", "+8", "+7", "+6", "+5",
	                                                                                           "+4", "+3", "+2", "+1"})
	public String enhancement;
	
	@SlashArgument( key = "item", text = "The item you wish to look up", required = true )
	public String item;
	
	@SlashArgument( key = "failstack", text = "Your current fail stack" )
	public int failstack;
	
	@Override
	public String getDescription()
	{
		return "Allows you to look up stat changes and chances for enhancing items";
	}
	
	//TODO Add a system to allow giving a specific fs and it will show enhancement chance range above and below that value
	
	@Override
	public void slashCommandExecuted(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
	{
		super.item = item;
		super.enhancement = enhancement;
		
		super.slashCommandExecuted(slashEvent, guild, author, channel, message);
	}
	
	public void showInfo(BotChannel channel,  String object, int id, int enhance_level, Message ms) {

		BDOItem sItem = BDOItemUtils.getItem(id);
		
		int currentFailStack = -1;
		
		if(sItem == null || sItem.data == null){
			ChatUtils.sendEmbed(channel, "Unable to retrieve the item info. This may be due to an issue on the website. Please try again later");
			ChatUtils.deleteMessage(ms);
			return;
		}
		
		BDOItemData itemData = sItem.data;
		
		if(itemData != null){
			EmbedBuilder builder = new EmbedBuilder();
			String name = BDOItemUtils.processItemName(itemData, itemData.name, enhance_level);
			int enhance = BDOItemUtils.getEnhanceIndex(itemData, enhance_level);
			BDOItemEnhancement enhancement = BDOItemUtils.getEnhanceObject(itemData, enhance - 1);
			
			if(enhance <= 0){
				ChatUtils.sendEmbed(channel, "You need to give a enhancement level higher then 0 to use this command");
				ChatUtils.deleteMessage(ms);
				return;
			}
			
			if(itemData.grade == 0){
				builder.setColor(new Color(189, 189, 189));
			}else if (itemData.grade == 1){
				builder.setColor(new Color(128, 161, 66));
			}else if (itemData.grade == 2){
				builder.setColor(new Color(60, 124, 178));
			}else if (itemData.grade == 3){
				builder.setColor(new Color(234, 178, 56));
			}else if (itemData.grade == 4){
				builder.setColor(new Color(189, 86, 66));
			}
			
			builder.setTitle(name, BDOItemIndex.BASE_URL + "us/item/" + id + "/#" + enhance);
			
			if(itemData.icon != null){
				builder.setThumbnail(itemData.icon);
			}
			
			StringBuilder sBuilder = new StringBuilder();
			
			if(enhancement != null){
				BDOItemEnhancement newEnhancement = BDOItemUtils.getEnhanceObject(itemData, enhance);
				
				if(newEnhancement != null) {
					StringBuilder sBuilder1 = new StringBuilder();
					StringBuilder sBuilder2 = new StringBuilder();
					
					displayStats(enhancement, sBuilder1);
					displayStats(newEnhancement, sBuilder2);
					
					if (!sBuilder1.toString().isBlank() && !sBuilder2.toString().isBlank()) {
						builder.addField("Stats", sBuilder1.toString(), true);
						
						builder.addField(EmbedBuilder.ZERO_WIDTH_SPACE, StringUtils.repeat("**-->**\n", StringUtils.countMatches(
								sBuilder1.toString().strip(), "\n") + 1), true);
						
						builder.addField(EmbedBuilder.ZERO_WIDTH_SPACE, sBuilder2.toString(), true);
					}
					
					LinkedHashMap<String, ArrayList<String>> map1 = enhancement.itemEffects;
					LinkedHashMap<String, ArrayList<String>> map2 = newEnhancement.itemEffects;
					
					if(map1 != null && map2 != null){
						if(map1.containsKey("Item Effect") && map2.containsKey("Item Effect")){
							StringBuilder effectBuilder1 = new StringBuilder();
							StringBuilder effectBuilder2 = new StringBuilder();
							
							for(String t : map1.get("Item Effect")){
								if(map2.get("Item Effect").contains(t)) continue;
								effectBuilder1.append(t.replace("\n", "") + "\n");
							}
							
							for(String t : map2.get("Item Effect")){
								if(map1.get("Item Effect").contains(t)) continue;
								effectBuilder2.append(t.replace("\n", "") + "\n");
							}
							
							if(!effectBuilder1.toString().equals(effectBuilder2.toString())) {
								if (!effectBuilder1.toString().isBlank() && !effectBuilder2.toString().isBlank()) {
									builder.addField("Item effects", effectBuilder1.toString(), true);
									
									String[] llLines = effectBuilder1.toString().split("\n");
									StringBuilder lines = new StringBuilder();
									
									for(int i = 0; i < StringUtils.countMatches(effectBuilder1.toString(), "\n"); i++){
										lines.append("**-->**\n" + (llLines.length > i && llLines[i].length() > 25 ? EmbedBuilder.ZERO_WIDTH_SPACE + "\n" : ""));
									}
									
									builder.addField(EmbedBuilder.ZERO_WIDTH_SPACE, lines.toString(), true);
									builder.addField(EmbedBuilder.ZERO_WIDTH_SPACE, effectBuilder2.toString(), true);
								}
							}
						}
					}
					
					if (itemData.EU_marketPrice != null || itemData.NA_marketPrice != null) {
						StringBuilder priceBuilder1 = new StringBuilder();
						StringBuilder priceBuilder2 = new StringBuilder();
						
						DecimalFormat formatter = (DecimalFormat)NumberFormat.getInstance(Locale.US);
						DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();
						
						symbols.setGroupingSeparator(',');
						formatter.setDecimalFormatSymbols(symbols);
						
						if (itemData.EU_marketPrice != null) {
							Long prePrice = BDOItemUtils.getItemPrice(itemData.EU_marketPrice, enhance - 1);
							Long postPrice = BDOItemUtils.getItemPrice(itemData.EU_marketPrice, enhance);
							
							if(prePrice != null) priceBuilder1.append("**EU**: *" + formatter.format(prePrice) + "* Silver\n");
							if(postPrice != null) priceBuilder2.append("**EU**: *" + formatter.format(postPrice) + "* Silver\n");
						}
						
						if (itemData.NA_marketPrice != null) {
							Long prePrice = BDOItemUtils.getItemPrice(itemData.NA_marketPrice, enhance - 1);
							Long postPrice = BDOItemUtils.getItemPrice(itemData.NA_marketPrice, enhance);
							
							if(prePrice != null) priceBuilder1.append("**NA**: *" + formatter.format(prePrice) + "* Silver\n");
							if(postPrice != null) priceBuilder2.append("**NA**: *" + formatter.format(postPrice) + "* Silver\n");
						}
						
						
						builder.addField("Item price", priceBuilder1.toString(), true);
						
						builder.addField(EmbedBuilder.ZERO_WIDTH_SPACE, StringUtils.repeat("**-->**\n", StringUtils.countMatches(
								priceBuilder1.toString().strip(), "\n") + 1), true);
						
						builder.addField(EmbedBuilder.ZERO_WIDTH_SPACE, priceBuilder2.toString(), true);
					}
				}
					
					//sBuilder.append("**Base Enhancement Chance**: *"  + enhancement.chance + "%*\n\n")
				currentFailStack = failstack;
				
					if (!newEnhancement.usesFS) {
						DecimalFormat df = new DecimalFormat(newEnhancement.chance >= 5 ? "#.#" : newEnhancement.chance >= 0.5 ? "#.##" : "#.###");
						df.setRoundingMode(RoundingMode.CEILING);
						
						sBuilder.append("*Enhancement Chance*: **").append(
								df.format(newEnhancement.chance).replace(",", ".")).append("**%\n");
						
						sBuilder.append("\n*__Does not use FS when enhancing.__*");
					} else {
						if(currentFailStack != -1){
							for (int i = 0; i < 5; i++) {
								int increment = BDOItemUtils.getFSIncrement(1, newEnhancement.chance);
								int num = i == 2 ? currentFailStack : i <= 1 ? currentFailStack - ((2 - i) * increment) : currentFailStack + ((i - 2) * increment);
								double chance = BDOItemUtils.getEnhanceChance(num, newEnhancement.chance);
								
								if(num >= 0) {
									DecimalFormat df = new DecimalFormat(chance >= 5 ? "#.#" : chance >= 0.5 ? "#.##" : "#.###");
									df.setRoundingMode(RoundingMode.CEILING);
									
									sBuilder.append((i == 2 ? "__" : "") + "*Enhancement Chance at* **").append(num).append("** FS: **").append(df.format(chance).replace(",", ".")).append(
											"**%" + (i == 2 ? "__" : "") + "\n");
									
									if (chance >= 70) {
										break;
									}
								}
							}
						}else {
							for (int i = 0; i < 5; i++) {
								int num = i * (BDOItemUtils.getFSIncrement(1, newEnhancement.chance));
								double chance = BDOItemUtils.getIncrementedChance(num, newEnhancement.chance);
								
								DecimalFormat df = new DecimalFormat(chance >= 5 ? "#.#" : chance >= 0.5 ? "#.##" : "#.###");
								df.setRoundingMode(RoundingMode.CEILING);
								
								sBuilder.append("*Enhancement Chance at* **").append(BDOItemUtils.getFSIncrement(num, newEnhancement.chance)).append(
										"** FS: **").append(df.format(chance).replace(",", ".")).append("**%\n");
								
								if (chance >= 70) {
									break;
								}
							}
						}
				}
				
				sBuilder.append("\n");
				
				String requiredItem = enhancement.requiredItem;
				
				if(requiredItem != null) {
					HashMap<String, Integer> keyList = new HashMap<>();
					BDOItemIndex.ITEM_INDEX.forEach((s1, s2) -> keyList.put(s2, s1));
					
					if (keyList.containsKey(requiredItem)) {
						requiredItem = "[" + requiredItem + "](" + BDOItemIndex.BASE_URL + "us/item/" + keyList.get(requiredItem) + "/)";
					}
					
					sBuilder.append(
							"**Required item**: **" + enhancement.requiredItemAmount + "x** **" + requiredItem + "**\n");
				}
				
				if(enhancement.cron_cost > 0) sBuilder.append("**Cron stones required**: *").append(
						enhancement.cron_cost).append("*\n");
				
				String sameEnhance = "It can be enhanced with ";
				boolean foundSame = false;
				
				for(String t : itemData.description){
					if(t.startsWith(sameEnhance)){
						sBuilder.append("**__" + t.substring(2).trim() + "__**");
						foundSame = true;
					}
				}
				
				if(!foundSame && enhancement.durabilityLoss != 0){
					sBuilder.append("__*Durability loss*: **" + enhancement.durabilityLoss + "**__\n");
				}
				
				sBuilder.append("\n");
			}
			
			if(channel instanceof SlashCommandChannel){
				SlashCommandChannel slashCh = (SlashCommandChannel)channel;
				
				if(slashCh.event instanceof SlashCommandInteractionEvent){
					SlashCommandInteractionEvent event = (SlashCommandInteractionEvent)slashCh.event;
					event.deferReply(true);
				}
			}
			
			builder.setDescription(sBuilder.toString());
			builder.setFooter(EmbedBuilder.ZERO_WIDTH_SPACE);
			
			File gr = getGraphFile(itemData.enhancementLevels[enhance], currentFailStack);
			
			if (gr != null) {
				builder.setImage("attachment://graph.png");
			}
			
			if(ms != null) {
				ChatUtils.setEmbedColor(ms.getGuild(), ms.getAuthor(), builder);
				ChatUtils.setFooter(builder, ms.getAuthor(), channel);
			}
			
			
			User user = null;
			Guild guild = null;
			
			if(channel instanceof SlashCommandChannel){
				SlashCommandChannel slashCh = (SlashCommandChannel)channel;
				
				if(slashCh.event instanceof SlashCommandInteractionEvent){
					SlashCommandInteractionEvent event = (SlashCommandInteractionEvent)slashCh.event;
					
					user = event.getUser();
					guild = event.getGuild();
				}
			}else if(ms != null){
				user = ms.getAuthor();
				guild = ms.getGuild();
			}
			
			SlashMessageBuilder slashBuilder = ChatUtils.createSlashMessage(user, channel);
			slashBuilder.withEmbed(builder);
			slashBuilder.withAttachment(gr, "graph.png");
			slashBuilder.send();
		
		}else{
			if(ms == null){
				ChatUtils.sendMessage(channel, "Found no item with that name!");
			}else {
				ChatUtils.editMessage(ms, "Found no item with that name!");
			}
		}
	}
	
	public static File getGraphFile(BDOItemEnhancement enhancement, int currentFs){
		if(enhancement.chance >= 100 || !enhancement.usesFS) return null;
		
		int width = 900;
		int height = 512;
		
		int padding = 30;
		int labelPadding = 30;
		
		BufferedImage bufferedImage = new BufferedImage(width, height * 2, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = bufferedImage.createGraphics();
		
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, width, height);
		g2.setColor(Color.BLACK);
		
		ArrayList<Point2D> scores = new ArrayList<>();
		ArrayList<Point2D> secondScores = new ArrayList<>();
		
		int max = 40;
		
		for (int i = 0; i < max; i++) {
			double chance = BDOItemUtils.getIncrementedChance(i, enhancement.chance);
			scores.add(new Double(BDOItemUtils.getFSIncrement(i, enhancement.chance), Math.min(100, chance)));
			if (chance >= 100 && i >= 5) {
				break;
			}
		}
		
		
		for (int i = scores.size() >= max ? max / 8 : 0; i < max; i++) {
			double chance = BDOItemUtils.getIncrementedChance(i, enhancement.chance);
			double c = Math.round(100d / chance);
			
			secondScores.add(new Double(BDOItemUtils.getFSIncrement(i, enhancement.chance), c));
			
			if (c <= 1 && i >= 2) {
				break;
			}
		}
		
		renderGraph("Chance : Failstacks", width, height, padding, labelPadding, new Color(44, 102, 230, 180), 0, g2, scores, true, currentFs);
		renderGraph("Avg attempts : Failstacks", width, height, padding, labelPadding, new Color(230, 40, 40, 180), height, g2, secondScores, false, currentFs);
		
		g2.dispose();
		
		File file = new File("graph.png");
		
		try {
			ImageIO.write(bufferedImage, "png", file);
		} catch (IOException e) {
			Logging.exception(e);
		}
		
		return file;
	}
	
	private static void renderGraph(String title, int width, int height, int padding, int labelPadding, Color lineColor, int yOffset,
			Graphics2D g2, ArrayList<Point2D> scores, boolean perc, int currentFs)
	{
		g2.setFont(new Font("Uni Sans Heavy", Font.ITALIC,12));
		
		Color gridColor = new Color(200, 200, 200, 200);
		Color pointColor = new Color(100, 100, 100, 200);
		
		Stroke GRAPH_STROKE = new BasicStroke(3f);
		
		double maxScore = java.lang.Double.MIN_VALUE;
		
		for (Point2D score : scores) {
			maxScore = Math.max(maxScore, score.getY());
		}
		
		maxScore *= 1.25;
		
		if(!perc){
			maxScore = Math.round(maxScore);
		}
		
		if(maxScore == 1){
			maxScore += 1;
		}
		
		int numberYDivisions = 10;
		
		if(numberYDivisions > maxScore && maxScore > 1){
			numberYDivisions = (int)maxScore;
		}
		
		double xScale = ((double)width - (2 * padding) - labelPadding) / (scores.size() - 1);
		double yScale = ((double)height - 2 * padding - labelPadding) / (maxScore);
		
		List<Point> graphPoints = new ArrayList<>();
		
		for (int i = 0; i < scores.size(); i++) {
			int x1 = (int) (i * xScale + padding + labelPadding);
			int y1 = (int) ((maxScore - scores.get(i).getY()) * yScale + padding);
			graphPoints.add(new Point(x1, y1));
		}
		// draw white background
		g2.setColor(Color.gray);
		g2.fillRect(0, yOffset, width, height);
		
		g2.setColor(Color.darkGray);
		g2.drawRect(0, yOffset, width, height);
		
		g2.setColor(Color.white);
		g2.fillRect(padding + labelPadding, yOffset + padding, width - (2 * padding) - labelPadding, height - 2 * padding - labelPadding);
		g2.setColor(Color.BLACK);
		
		g2.setColor(new Color(150, 150, 150, 175));
		
		Stroke oldStroke = g2.getStroke();
		g2.setStroke(GRAPH_STROKE);
		
		for (int i = 1; i < graphPoints.size() - 1; i++) {
			int x1 = graphPoints.get(i).x;
			int y1 = yOffset + graphPoints.get(i).y - 10;
			int y2 = yOffset + height - padding - labelPadding;
			
			if((i % ((int) ((scores.size() / 20.0)) + 1)) == 0){
				g2.drawLine(x1, y1, x1, y2);
				//g2.drawLine(x1, y1, x1 + (graphPoints.get(0).x - x1), y1);
			}
		}
		
		g2.setStroke(oldStroke);
		
		int pointWidth = 7;
		
		// create hatch marks and grid lines for y axis.
		for (int i = 0; i < numberYDivisions + 1; i++) {
			int x0 = padding + labelPadding;
			int x1 = pointWidth + padding + labelPadding;
			int y0 = yOffset + height - ((i * (height - padding * 2 - labelPadding)) / numberYDivisions + padding + labelPadding);
			
			if (scores.size() > 0) {
				g2.setColor(gridColor);
				g2.drawLine(padding + labelPadding + 1 + pointWidth, y0, width - padding, y0);
				g2.setColor(Color.BLACK);
				
				double v = maxScore * ((i * 1.0) / numberYDivisions);
				double chance = perc ? v : Math.ceil(v);
				
				if(!perc || chance <= 100) {
					DecimalFormat df = new DecimalFormat(chance >= 5 ? "#.#" : chance >= 0.5 ? "#.##" : "#.###");
					df.setRoundingMode(RoundingMode.CEILING);
					
					String yLabel = perc ? df.format(chance).replace(",", ".") + "%" : (int)chance + "";
					FontMetrics metrics = g2.getFontMetrics();
					int labelWidth = metrics.stringWidth(yLabel);
					g2.drawString(yLabel, x0 - labelWidth - 5, y0 + (metrics.getHeight() / 2) - 3);
				}
			}
			
			
			g2.drawLine(x0, y0, x1, y0);
		}
		
		// and for x axis
		for (int i = 0; i < scores.size(); i++) {
			if (scores.size() > 1) {
				int x0 = i * (width - padding * 2 - labelPadding) / (scores.size() - 1) + padding + labelPadding;
				int y0 = yOffset + height - padding - labelPadding;
				int y1 = y0 - pointWidth;
				if ((i % ((int) ((scores.size() / 20.0)) + 1)) == 0 || ((int)scores.get(i).getX()) == currentFs) {
					g2.setColor(gridColor);
					g2.drawLine(x0, yOffset + height - padding - labelPadding - 1 - pointWidth, x0, yOffset + padding);
					g2.setColor(Color.BLACK);
					String xLabel = (int)scores.get(i).getX() + "";
					FontMetrics metrics = g2.getFontMetrics();
					int labelWidth = metrics.stringWidth(xLabel);
					g2.drawString(xLabel, x0 - labelWidth / 2, y0 + metrics.getHeight() + 3);
				}
				g2.drawLine(x0, y0, x0, y1);
			}
		}
		
		// create x and y axes
		g2.drawLine(padding + labelPadding, yOffset + height - padding - labelPadding, padding + labelPadding, yOffset + padding);
		g2.drawLine(padding + labelPadding, yOffset + height - padding - labelPadding, width - padding, yOffset + height - padding - labelPadding);
		
		oldStroke = g2.getStroke();
		g2.setColor(lineColor);
		g2.setStroke(GRAPH_STROKE);
		
		
		for (int i = 0; i < graphPoints.size() - 1; i++) {
			int x1 = graphPoints.get(i).x;
			int y1 = yOffset + graphPoints.get(i).y;
			int x2 = graphPoints.get(i + 1).x;
			int y2 = yOffset + graphPoints.get(i + 1).y;
			g2.drawLine(x1, y1, x2, y2);
		}
		
		g2.setStroke(oldStroke);
		g2.setColor(pointColor);
		
		for (int i = 1; i < graphPoints.size() - 1; i++) {
			g2.fillOval(graphPoints.get(i).x - pointWidth / 2, yOffset + graphPoints.get(i).y - pointWidth / 2, pointWidth, pointWidth);
		}
		
		for (int i = 1; i < graphPoints.size() - 1; i++) {
			if((i % ((int) ((scores.size() / 20.0)) + 1)) == 0) {
				int x = graphPoints.get(i).x - pointWidth / 2;
				int y = yOffset + graphPoints.get(i).y - pointWidth / 2;
				
				DecimalFormat df = new DecimalFormat(scores.get(i).getY() >= 5 ? "#.#" : scores.get(i).getY() >= 0.5 ? "#.##" : "#.###");
				df.setRoundingMode(RoundingMode.CEILING);
				
				String label = perc ? df.format(scores.get(i).getY()).replace(",", ".") + "%" : (int)scores.get(i).getY() + "";
				
				FontMetrics metrics = g2.getFontMetrics();
				int labelWidth = metrics.stringWidth(label);
				
				int ovalW = labelWidth + 5;
				int ovalH = metrics.getHeight();
				
				if(scores.get(i).getX() == currentFs){
					g2.setColor(Color.orange);
				}else{
					g2.setColor(lineColor);
				}
				g2.fillOval(x + 1 - (labelWidth / 2), y - 23, ovalW, ovalH);
				
				g2.setColor(Color.BLACK);
				g2.drawString(label, x + 4 - (labelWidth / 2), y - 10);
			}
		}
		g2.setFont(new Font("Uni Sans Heavy", Font.BOLD,20));
		FontMetrics metrics = g2.getFontMetrics();
		int labelWidth = metrics.stringWidth(title);
		g2.drawString(title , (width / 2) - (labelWidth / 2), yOffset + padding - 5);
	}
	
	@Override
	public String getSlashName()
	{
		return "enhance";
	}
}
