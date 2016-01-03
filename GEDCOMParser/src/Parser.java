
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class Parser {

    private static HashMap<String, Individual> individuals = new HashMap<String, Individual>();
    private static HashMap<String, Family> families = new HashMap<String, Family>();

    // Extract parents of a person
    public static Individual getFatherOf(Individual individual) {
        for (Map.Entry<String, Family> entry : families.entrySet()) {
            Family family = entry.getValue();

            if (family.children.contains(individual)) {
                return family.husband;
            }
        }

        return null;
    }

    // Extract parents of a person
    public static Individual getMotherOf(Individual individual) {
        for (Map.Entry<String, Family> entry : families.entrySet()) {
            Family family = entry.getValue();

            if (family.children.contains(individual)) {
                return family.wife;
            }
        }

        return null;
    }

    // Extract the names
    public static void main(String[] args) throws Exception {
        if(args.length != 2) {
            System.out.println("Usage: ./Parser <input file.ged> <output file.csv>");
            return;
        }
        
        // Extract all individuals and families
        BufferedReader inFile = new BufferedReader(new FileReader(args[0]));
        String line = inFile.readLine();

        Individual individual = null;
        Family family = null;

        String currentLevel1Code = "";

        while (line != null) {
            String[] tokens = line.split(" ");
            int codeLevel = Integer.parseInt(tokens[0]);

            if (codeLevel == 1) {
                currentLevel1Code = tokens[1];
            }

            if (line.endsWith("INDI")) {
                // New individual detected, close the last individual
                if (individual != null) {
                    individuals.put(individual.id, individual);
                }

                // New individual
                individual = new Individual();
                individual.id = line.split(" ")[1];
            } else if (line.contains("GIVN")) {
                // First Name detected
                individual.firstName = line.substring(line.indexOf("GIVN") + 4, line.length()).trim();
            } else if (line.contains("SURN")) {
                // Last Name detected
                individual.lastName = line.substring(line.indexOf("SURN") + 4, line.length()).trim();
            } else if (line.contains("NSFX")) {
                // Name suffix detected
                individual.nameSuffix = line.substring(line.indexOf("NSFX") + 4, line.length()).trim();
            } else if (line.contains("SEX")) {
                individual.gender = line.substring(line.indexOf("SEX") + 3, line.length()).trim();
            } else if (line.contains("DATE")) {
                if (currentLevel1Code.equals("BIRT")) {
                    individual.birthDate = line.substring(line.indexOf("DATE") + 4, line.length()).trim();
                } else if (currentLevel1Code.equals("DEAT")) {
                    individual.deathDate = line.substring(line.indexOf("DATE") + 4, line.length()).trim();
                } else if (currentLevel1Code.equals("MARR")) {
                    family.marriageDate = line.substring(line.indexOf("DATE") + 4, line.length()).trim();
                }
            } else if (line.contains("PLAC")) {
                if (currentLevel1Code.equals("BIRT")) {
                    individual.birthPlace = line.substring(line.indexOf("PLAC") + 4, line.length()).trim();
                } else if (currentLevel1Code.equals("DEAT")) {
                    individual.deathPlace = line.substring(line.indexOf("PLAC") + 4, line.length()).trim();
                } else if (currentLevel1Code.equals("BURI")) {
                    individual.burialPlace = line.substring(line.indexOf("PLAC") + 4, line.length()).trim();
                } else if (currentLevel1Code.equals("MARR")) {
                    family.marriagePlace = line.substring(line.indexOf("PLAC") + 4, line.length()).trim();
                }
            } else if (line.contains("CAUS")) {
                individual.deathCause = line.substring(line.indexOf("CAUS") + 4, line.length()).trim();
            } else if (line.endsWith("FAM")) {
                // New family detected, close the last family, store the last person because we are
                // sure the rest of the line are individuals
                if (individual != null) {
                    individuals.put(individual.id, individual);
                    individual = null;
                }

                if (family != null) {
                    families.put(family.id, family);
                }

                family = new Family();
                family.id = line.split(" ")[1].trim();
            } else if (line.contains("HUSB")) {
                family.husband = individuals.get(line.split(" ")[2]);
            } else if (line.contains("WIFE")) {
                family.wife = individuals.get(line.split(" ")[2]);
            } else if (line.contains("CHIL")) {
                family.children.add(individuals.get(line.split(" ")[2]));
            }

            line = inFile.readLine();
        }

        // Record the last family
        families.put(family.id, family);

        inFile.close();

        // Okay now write to a CSV delimited
        PrintWriter outFile = new PrintWriter(new FileWriter(args[1]));
        outFile.println("\"First Name\",\"Last Name\",\"Name Suffix\",\"Gender\",\"Date of Birth\",\"Place of Birth\",\"Date of Death\",\"Place of Death\",\"Cause of Death\",\"Place of Burial\",\"Father's Name\",\"Mother's Name\"");

        for (Map.Entry<String, Individual> entry : individuals.entrySet()) {
            individual = entry.getValue();
            Individual father = getFatherOf(individual);
            Individual mother = getMotherOf(individual);

            outFile.print("\"" + individual.firstName + "\",");
            outFile.print("\"" + individual.lastName + "\",");
            outFile.print("\"" + individual.nameSuffix + "\",");
            outFile.print("\"" + individual.gender + "\",");
            outFile.print("\"" + individual.birthDate + "\",");
            outFile.print("\"" + individual.birthPlace + "\",");
            outFile.print("\"" + individual.deathDate + "\",");
            outFile.print("\"" + individual.deathPlace + "\",");
            outFile.print("\"" + individual.deathCause + "\",");
            outFile.print("\"" + individual.burialPlace + "\",");

            if (father == null) {
                outFile.print("\"\",");
            } else {
                outFile.print("\"" + father.firstName + " " + father.lastName + " " + father.nameSuffix + "\",");
            }

            if (mother == null) {
                outFile.print("\"\"");
            } else {
                outFile.print("\"" + mother.firstName + " " + mother.lastName + " " + mother.nameSuffix + "\",");
            }

            outFile.println();
        }

        outFile.close();
    }
}
