/*
 *  JavaXMPPBot - XMPP(Jabber) bot written in Java
 *  Copyright 2010 Mikhail Telnov <michael.telnov@gmail.com>
 *
 *  This file is part of JavaXMPPBot.
 *
 *  JavaXMPPBot is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  JavaXMPPBot is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with JavaXMPPBot.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  $Id$
 *
 */
package com.devti.JavaXMPPBot;

import java.io.IOException;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import java.io.StringReader;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.DOMException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class PacketProcessor implements PacketListener {

    private final Bot bot;
    private final Logger logger;

    public PacketProcessor(Bot bot) {
        this.bot = bot;
        this.logger = bot.getLogger();
    }

    @Override
    public void processPacket(Packet packet) {
        try {
            String raw = packet.toXML();
            logger.info("Have got a new packet: " + raw);
            Document document = DocumentBuilderFactory.
                    newInstance().newDocumentBuilder().
                    parse(new InputSource(new StringReader(raw)));
            Node node = document.getFirstChild();
            if (node.getNodeName().equalsIgnoreCase("message")) {
                MessageProcessor mp = new MessageProcessor(bot, node);
                mp.start();
            } else if (node.getNodeName().equalsIgnoreCase("presence")) {
                if ((node.getAttributes().getNamedItem("type") != null)
                        && node.getAttributes().getNamedItem("type").
                        getTextContent().
                        equalsIgnoreCase("unavailable")) {
                    String from = node.getAttributes().getNamedItem("from").
                            getTextContent();
                    String[] rooms = bot.getRooms();
                    for (int i = 0; i < rooms.length; i++) {
                        if (from.equalsIgnoreCase(rooms[i] + "/" + bot.getResource())) {
                            bot.leaveRoom(rooms[i]);
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                logger.warn("Sleep has been interrupted: "
                                        + e.getLocalizedMessage());
                            }
                            bot.joinRoom(rooms[i]);
                        }
                    }
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException |
                DOMException e) {
            logger.warn("An error occurred during packet process: " +
                    e.getLocalizedMessage());
        }
    }

}
