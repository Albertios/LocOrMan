package de.unimuenster.ifgi.locormandemo.eventbus;

/**
 * Created by sven on 07.09.16.
 */
public class AppWantsToSendExperimentUpdateReceivedResponseEvent {
    public final String messagePayloadString;

    public AppWantsToSendExperimentUpdateReceivedResponseEvent(String messageString) {
        messagePayloadString = messageString;
    }
}

/*final
Es können Klassen, Methoden, Attribute und Parameter als final bezeichnet (deklariert) werden. Einfach ausgedrückt bedeutet final in Java "du kannst mich jetzt nicht überschreiben".

Für finale Klassen bedeutet dies, dass man von ihr nicht erben kann (man kann keine Unterklasse erzeugen). Sie kann also nicht als Vorlage für eine neue Klasse dienen. Grundlegende Klassen, wie zum Beispiel die String-Klasse sind final. Wenn sie es nicht wäre, dann könnte man von ihr erben und ihre Methoden überschreiben und damit das Verhalten der erweiterten Klasse verändern.

Finale Methoden können in Subklassen nicht überschrieben werden.

Finale Attribute und auch Klassen-Variablen können nur ein einziges Mal zugewiesen werden. Sobald die Zuweisung erfolgt ist, kann eine finale Variable ihren Wert nicht mehr ändern. Bei Member-Variablen muss die Zuweisung bei der Instanzierung, bei Klassen-Variablen beim Laden der Klasse erfolgen.

Finale Parameter können ausschliesslich den beim Methodenaufruf übergebenen Wert besitzen. In der Methode selbst lassen sie sich nicht überschreiben.

*/