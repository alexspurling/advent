import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Day12Part2 {

    private record SpringRecord(int recordIdx, String input, int[] checkCodes) {

    }

    private static boolean codesMatch(String input, int[] codes) {
        int codeIdx = 0;
        int curCode = -1;
        for (int c = 0; c < input.length(); c++) {
            char ch = input.charAt(c);
            if (ch == '#') {
                if (curCode == -1) {
                    // We found the start of a new spring
                    if (codeIdx >= codes.length) {
                        // There are no more springs expected
//                        System.out.println("Permutation: " + input + ", codes " + Arrays.toString(codes) + " match: false; found a spring at " + c + " but expected none");
                        return false;
                    }
                    curCode = codes[codeIdx];
                    codeIdx++;
                } else if (curCode == 0) {
                    // We found a spring but we expected a .
//                    System.out.println("Permutation: " + input + ", codes " + Arrays.toString(codes) + " match: false; found a spring at " + c + " but expected .");
                    return false;
                }
                curCode -= 1; // Consume one #
            } else {
                if (curCode > 0) {
                    // We hit a . but were expecting curCode #s
//                    System.out.println("Permutation: " + input + ", codes " + Arrays.toString(codes) + " match: false; found a . at " + c + " but expected #");
                    return false;
                } else if (curCode == 0) {
                    // We hit a . which we were expecting
                    // Set curCode to -1 to indicate we're no longer looking for either . or #
                    curCode -= 1;
                }
            }
        }
        if (curCode > 0) {
            // We hit the end but were expecting more #s
//            System.out.println("Permutation: " + input + ", codes " + Arrays.toString(codes) + " match: false; hit the end but were expecting another code");
            return false;
        }
        if (codeIdx < codes.length) {
            // We hit the end but were expecting more #s
//            System.out.println("Permutation: " + input + ", codes " + Arrays.toString(codes) + " match: false; hit the end but were expecting another code");
            return false;
        }
        System.out.println("Permutation: " + input + ", codes " + Arrays.toString(codes) + " match: true");
        return true;
    }

    private static List<String> permutations(char[] input) {
        System.out.println("Input: " + new String(input));
        List<String> result = new ArrayList<>();
        permutationsHelper(input, 0, result);
        return result;
    }

    private static void permutationsHelper(char[] input, int index, List<String> result) {
        if (index == input.length) {
            result.add(new String(input));
            return;
        }

        if (input[index] == '?') {
            input[index] = '#';
            permutationsHelper(input, index + 1, result);
            input[index] = '.';
            permutationsHelper(input, index + 1, result);
            input[index] = '?';
        } else {
            permutationsHelper(input, index + 1, result);
        }
    }

    private static boolean contains(char[] input, char search) {
        for (char c: input) {
            if (c == search) {
                return true;
            }
        }
        return false;
    }

    private static boolean contains(char[] input, char search, int startIdx, int endIdx) {
        for (int c = startIdx; c < endIdx; c++) {
            if (input[c] == search) {
                return true;
            }
        }
        return false;
    }

    private record CacheKey(char[] input, int[] codes, int inputIdx, int codesIdx) {

    }

    private static final Map<CacheKey, Long> cache = new ConcurrentHashMap<>();

    private static long numSolutions(char[] input, int[] codes, int inputIdx, int codesIdx) {

        if (inputIdx == input.length) {
            return codesIdx == codes.length ? 1 : 0;
        }

        if (codesIdx == codes.length) {
            // If there are no more codes left but our pattern has hashes then this input is invalid
            return contains(input, '#', inputIdx, input.length) ? 0 : 1;
        }

        CacheKey cacheKey = new CacheKey(input, codes, inputIdx, codesIdx);
        Long cachedValue = cache.get(cacheKey);
        if (cachedValue != null) {
            return cachedValue;
        }

        char curChar = input[inputIdx];
        long numSolutions = 0;
        if (curChar == '.' || curChar == '?') {
            numSolutions += numSolutions(input, codes, inputIdx + 1, codesIdx);
        }
        if (curChar == '#' || curChar == '?') {
            int curCode = codes[codesIdx];
            // In order for nextCode to match the current input, the input must be at least that long
            // and consist of either # or ? chars
            if (curCode <= (input.length - inputIdx) && !contains(input, '.', inputIdx, inputIdx + curCode) &&
                    (curCode == (input.length - inputIdx) || input[inputIdx + curCode] != '#')) {
                int nextInputIdx;
                if (curCode == (input.length - inputIdx)) {
                    nextInputIdx = inputIdx + curCode;
                } else {
                    nextInputIdx = inputIdx + curCode + 1;
                }
                numSolutions += numSolutions(input, codes, nextInputIdx, codesIdx + 1);
            }
        }

        cache.put(cacheKey, numSolutions);

        return numSolutions;
    }

    private static String[] duplicate(String[] arr, int n) {
        String[] duplicatedArray = new String[arr.length * n];

        for (int i = 0; i < n; i++) {
            System.arraycopy(arr, 0, duplicatedArray, i * arr.length, arr.length);
        }

        return duplicatedArray;
    }

    public static void main(String[] args) {
        AtomicInteger recordIdx = new AtomicInteger();
        List<SpringRecord> records = Arrays.stream(day12input.split("\n")).map((line) -> {
            String[] record = line.split(" ");
            String unfoldedRecord = String.join("?", Collections.nCopies(5, record[0]));
            String[] codesStr = duplicate(record[1].split(","), 5);
            int[] codes = Arrays.stream(codesStr).mapToInt(Integer::parseInt).toArray();
            return new SpringRecord(recordIdx.incrementAndGet(), unfoldedRecord, codes);
        }).toList();

        long startTime = System.currentTimeMillis();
        final AtomicLong totalSolutionsAtomic = new AtomicLong();
//        final AtomicInteger nAtomic = new AtomicInteger();

        records.stream().parallel().forEach((record) -> {
            long numSolutions = numSolutions(record.input.toCharArray(), record.checkCodes, 0, 0);
            System.out.println(numSolutions + " (" + record.recordIdx + " / " + records.size() + ")");
            totalSolutionsAtomic.addAndGet(numSolutions);
        });
        System.out.println("Total: " + totalSolutionsAtomic.get() + " in " + (System.currentTimeMillis() - startTime) + "ms");
        System.out.println("Final cache size: " + cache.size());

        // 11461095383315
        // 11461095383315
        // 11461095383315

//        int totalSolutions = 0;
//        int n = 0;
//
//        for (SpringRecord record : records) {
//            n += 1;
//            if (n == 20) {
//                break;
//            }
//            int numSolutions = numSolutions(record.input.toCharArray(), record.checkCodes, 0, 0);
//            System.out.println(numSolutions + " (" + n + " / " + records.size() + ")");
//            totalSolutions += numSolutions;
//        }
//        totalSolutions += numSolutions(records.get(0).input.toCharArray(), records.get(0).checkCodes, 0, 0);
//        System.out.println("Total: " + totalSolutions + " in " + (System.currentTimeMillis() - startTime) + "ms");

//        SpringRecord record = records.get(5);
//        long count = 0;
//        for (String perm : permutations(record.input.toCharArray())) {
//            System.out.println(perm);
//            count += 1;
//        }
//
//        System.out.println("Count: " + count);
    }

    private static final String exampleInput = """
            ???.### 1,1,3
            .??..??...?##. 1,1,3
            ?#?#?#?#?#?#?#? 1,3,1,6
            ????.#...#... 4,1,1
            ????.######..#####. 1,6,5
            ?###???????? 3,2,1
            """;

    private static final String day12input = """
        ??#??#??##.#???? 4,2,2,1,2
        .#?#.???#?.?? 3,3,1
        #???.#???#?.?.??.? 2,1,5,1,1
        ??#??#???????? 1,5,1
        ?..##????#???#? 1,8,1
        ?#?##??#?#?#???? 12,1
        ?#???##???.??????? 7,2,1
        ?###??????.?#?.# 7,2,1,1
        ?.??.???.?? 2,2
        ??##?##?.###???##?#? 1,2,2,8,2
        #.#?.??.??#???#? 1,1,1,3,3
        .#???#.?#????# 5,1,2,1
        ???#???..?????#?#. 5,8
        ##?#??.????#?????? 6,5,1,2
        ??.?#??##????# 1,8,1
        ??.????.????.?#..? 1,1
        .??????...?##?.?. 2,2,4
        ??.???#???#?#?#???? 1,1,7,1,1,1
        #?.?##.??. 1,2,1
        #?????#??#.??.. 1,1,2,1,1
        ??#???.??#? 6,1,1
        #?..?.????.?? 1,1,3,1
        ??#??.?#?.#??#.?.#?. 5,2,1,1,2
        .????#??.#??????. 7,7
        ??#?.?#??. 4,2,1
        #?.?.#??.?????# 2,1,1,1,1
        ???????????. 2,2
        .??##????? 3,1
        #.?.??.????#?##? 1,2,8
        ??#.?.#?##????. 2,7
        ?#?.??????.????#??? 2,1,1,1,8
        ?????#??.?#?.#?. 1,5,1,1
        ??#?#?????.???. 3,3,1
        .?.???.??## 1,1,4
        .##?..##?.???# 2,3,3
        .##??????.# 2,4,1
        ?#??..#????? 2,1,1
        ?###?.????? 4,1
        ??#.#???#??#?#?## 1,1,1,10
        ??#???.??..##...##?? 1,3,1,2,3
        ##?????????.?#?.???. 6,3,2,1,1
        ##?.??.???# 3,1,1
        .??????#?#?.???##?? 10,5
        ??????..?#? 1,1,3
        #?##?#...?##?#?#??? 6,3,5
        ?.?.??????#?#? 1,7,1
        ????.###??????#??#?? 2,13
        ???.?????.??. 2,1
        ?#?????.???#???#??? 3,1,1,1,1,2
        ?.?.?????#?#?#? 1,8
        .#??#?#?.????????.? 7,2,1,1,1
        ?...??.?##?????#??? 3,2
        ??.?###?#??.?????? 1,4,1,1,1,2
        ???.????????????###? 1,2,10
        ???.???????#???? 3,3,1,3
        ?#??#?#????#.?? 1,7,1,2
        ??#????#?#?#?#??.##? 1,2,11,2
        ..??.????#? 1,2,1
        .?#?.???.????? 1,3,1
        ?#?#???#?#???.??#?? 12,3
        ??????##???###?#???? 1,1,3,11
        ?#?.??.???????? 2,1,1,2
        ??.?#??????#?? 1,4,2
        .??.?#??#?. 1,6
        ??.????.???? 1,1,2,2
        #.#????#??? 1,2,3
        ..??#?###????.??.? 11,1
        ???#?.??#???. 1,2,2,1
        ?#?##???#..#???? 5,3,1,1
        .???.#??#?? 1,1,2
        ????#?.?????????.. 2,8
        .?#??##.??? 6,1
        ?????#???. 1,5,1
        .#?#?#??????# 3,5,1
        ?.???#???#????????? 4,2,5
        #??????#?#.?.#? 1,1,3,1,1
        #??????###??.?????? 12,1,2
        ???#??#?.#?###.??. 1,2,1,5,2
        .???.?#???##?#?????? 1,1,6,1,1
        ??..?.???#??. 1,1
        ???.?#?##?.???#??#?? 1,4,7,1
        ????#??#??#..??? 9,1,2
        .?.#??.?????#??? 1,2,8
        #??#?..?###?#?????# 1,1,9,1
        ???..?.?#?. 1,1,2
        ????##?????#?.??? 6,3,1
        ..#??#?????.???? 7,3
        ?.??#?#??##?.#... 8,1
        ?##??.????#.?????? 4,1,1,1,1,1
        .?????#???#?#?.?. 1,3,4,1
        #?##??#?#.#...??? 7,1,1,2
        ??.????.?????###? 1,2,7
        ?.##????#????.??. 7,1
        ??????#???#????? 1,6,1,1
        ?#.??#??#?#?? 1,1,1,5
        ?#????#.#??? 1,4,2,1
        #??.?.?????. 3,2,1
        ????????#?.? 4,3
        #.??#?#????.?#????? 1,3,1,3,1,4
        ???#???#?.#?.#. 2,2,1,1,1
        ?.#????..##?????.? 4,7
        ??#???????? 3,2
        ??..?...?##?? 1,4
        #??#?.??#?? 5,1
        ?#??#???????????##?? 3,1,1,2,1,2
        ?????????.#.. 3,2,1
        .??????##??? 1,7
        ?.??.??.#??. 1,1,1
        ???#????#??????????? 1,3,1,7,1
        ?#?.#??.????# 2,2,2,2
        ???#?#???#???. 8,1
        ???###.?#?#??#? 1,3,3,2
        ?.#?????#??? 4,1,1
        ????.?#??#??#?? 1,8
        .??..???##????#?? 1,6,1,1
        ?##?##??#??#??#???#. 5,8,3
        ?..?#?.????? 1,3,1,2
        ?##.?#????? 3,4
        ??#??#????## 5,1,2
        #..?#??..#? 1,3,1
        ?#?#?.???###.# 1,1,5,1
        ?????#?????.. 5,1,1
        ?#??#.?.?? 4,1
        ????#??#??.?#??? 5,2,4
        #??#?##??.????.???? 8,1,1,1
        ?#??#??#..??##..??.? 2,5,1,2,1,1
        ??##????####????? 6,9
        .??#??###??????#???# 1,8,1,1,1,1
        ??##?..??#???.?? 3,4
        ????#????????? 9,3
        ?##??.???..?? 3,1,2,1
        #?.???#??#????#.?? 2,9,1,1
        #.???#?#?#??????.??? 1,1,8,1,1,1
        #.?.??#####?#?????? 1,12
        .??###?##????.???#?. 11,3
        ?#????.?????#?.?? 5,2,1,2
        ??##?#????.??#.????? 7,1,1,1,1,1
        .??????.???. 2,2,1
        ??#?...?##??? 1,4
        ..?????.???#?#? 3,6
        ?.???????#?#?. 1,7
        ??#??.??.. 3,1,1
        ?.#?.????#?#??.??? 2,1,4,1,1,1
        #?##??#..? 1,5
        ?????????.#?? 5,1,1,2
        .??.?????.?????##? 5,7
        ?.#????#??? 1,5
        ????#???.????? 3,2,1,1
        .??.?##???#? 2,3,3
        .???##??.??.##???.?? 7,1,5
        ????.#???#? 1,1,3
        ??????##?### 1,6
        ?????#?##????? 6,1
        ?#??#.??.??. 4,2
        ?????.?????#???.#? 4,9,1
        ??.???????..?.. 1,1
        ?.??????##??#????. 1,5,2,2
        .??#?????? 1,3
        ??#?.???.?#?##??#?? 1,8
        ?.?#?#?##?#?#??. 1,9,2
        ?????.?.???#?#?##?? 3,1,9
        .??.?#?.#?##?. 1,1,5
        #??????##..#???.#??? 1,6,2,1,1
        #?.?.???#. 2,1,1
        ???#?#??????. 2,1,1,1
        ##??.?##.?.????#? 2,3,1,3,2
        ???.#?.?#????? 1,2,4
        #???.??#???. 1,1,4
        ?..????????#???.?. 1,1,1,5,2,1
        ????????.??..?.? 4,1
        ?.?????.... 1,1
        .?#????..###?????#? 2,1,1,3,1,1
        ..#.??#??????#?#?? 1,12
        .??#??##??.????#. 7,5
        ?##??#?#??.?#???. 9,2,1
        ?#?#?#???.#?#.#? 8,1,1,2
        ?.??.???.#?? 1,1,1,1
        ??????.?.#?? 5,2
        ???#????.????????? 1,1,1,1,8
        ??????##??#?????? 14,1
        ???.#.?.???????####? 1,2,4
        .#.??#??#???.#??#??. 1,6,2,1,1
        .????#??????### 5,1,5
        .??..#?..? 2,2
        ???#?????#..??# 6,2,3
        ??????#?##.?#??#.??. 7,5
        ????????##??.#? 2,2,3,1,1
        ?..###?#??..??..#??? 5,1
        #????.??##?##?? 1,1,1,7
        #?????#???????. 1,1,4,2
        ??????.??????#. 1,1,1,3
        ???#..???????# 2,1,8
        #.??#??..???# 1,3,1,4
        ????##?????##??.?#? 1,4,1,1,4,2
        ?.???#?????.??? 1,1,1,1,2
        .???????#? 1,1,2
        .??#?.???.# 3,1
        ...?#??????#.#?? 1,1,1,2,2
        ??#?.????.??## 3,2,1,4
        ??#?###??????. 7,1,1
        ??.?????#?????.?#?.. 1,5,3,3
        ..??##?????.#??? 4,2,1
        ??#.????#??#.??? 2,1,2,1,1
        ???#??.???.??#? 1,3,1,1,4
        ?#???###????.??? 9,1,1,1
        ?...?.??#.????? 1,2,2
        ?#????##.?#???? 2,5,1,2
        ?#??#??#??.?.??.?##. 2,6,1,2,2
        ..??#?.##???? 1,2,2,1
        ##?..#?#?..#??? 2,3,2,1
        ??.??.??#????? 1,3,3
        .??#????#?? 2,1,2
        .?##?.#?##?..#? 4,4,1
        .??.?????? 1,1,1
        ???..???##????. 1,2,2,1
        ??#??????#??? 2,7
        ???#.??????#???????# 1,1,1,5,3,1
        ???.?#???#?#??# 1,1,1,2,4
        ?..?#??.??##.???? 1,3,1,2,3
        ##??????#?.?.?.?? 2,1,3,1,1
        .?..???#???#??#### 1,13
        ?#??#..?.#?. 4,2
        ??#???.??? 3,2,2
        ..??##?#??..??? 6,3
        #.????????.??###?#?. 1,1,1,1,5,1
        ???.?..?## 1,1,2
        ?.?.???????.???????? 1,1
        ???.??????..?. 1,2,2,1
        ?.#????????###.?#. 1,3,1,2,3,1
        ??#?????##??#?? 11,1
        ??????#???? 1,2
        .##?.?????????#??? 3,4,2,4
        ?#???.#.??? 3,1,1
        .???#.???.#. 1,1,1,1
        ????.??#??#?#??.?? 1,2,6,1,1
        #?#?.#?????????#?# 1,2,1,2,5,1
        ??.#??????#?. 1,1,2,2
        #?.?.##...#??#?# 2,1,2,6
        ???#??..?? 6,1
        ?????????#?#???.???? 1,1,11,2
        ???#?#?#????..? 1,1,3,2,1
        ??#???#??????##?#.?? 17,1
        ????..???? 1,3
        ??????#???##???#? 2,1,7
        ####???.?? 5,1,1
        ?.???????#?#?##?##?? 1,1,1,7,2,1
        ???????.#?? 4,1,1
        ??.##???.#???#??#?? 1,2,1,1,6
        .?#????????.?#?..?. 4,4,2,1
        ??#?.??#??? 4,5
        #??????##??. 1,1,3
        #?????#.??#?.#?# 2,3,1,3
        ???????#?#?????????. 14,2
        .??.#?#???##??#.. 1,9
        ?????.?????#?#??? 2,1,6
        ?????.?..??#??.#??? 1,1,1,5,1,1
        .?.#??.?.??.? 3,1
        ?.##??###?????#???? 9,1,1,1
        ?#.#??????#..??#?? 2,1,4,4
        ?.?.??#??.??.??#?#? 1,5,5
        ..#??.???.?###?. 2,1,3
        ?????????? 3,1
        ?#????#??????#?? 2,4,7
        .??.???????#???. 1,8
        .?????#???????#??? 1,11
        ?.??.??????#???? 1,1,3,2,1
        .#.?##?#??#??# 1,5,2,2
        ??????##?#??#??.? 1,8,1,1
        ?#####?.##?#????.?? 7,4,3,1
        ?#????#??# 4,2,1
        ???#????.?. 1,5,1
        ??#???#???.? 2,2,1,1
        .#?#?#?#????..?? 1,1,1,1,3
        ??.????##??##?.?. 1,5,3,1
        ?#??????.???? 2,1,1,4
        ??.?.??????##???## 2,1,1,8
        .???.####??????#??. 1,4,6
        ..????#.?????????? 2,1,1,4,1
        .?##???????##????. 3,7
        ???##??#?????.????.. 9,1,4
        ..?.??.??????.#?.?# 1,2,1,2,2,1
        ?#????????##????#??? 1,2,6,1,1,1
        ????####???????? 2,7,3
        .?.????..?.?????. 4,1
        ???#???#?#???#?#? 2,11
        ??.#?????.?.# 1,5,1,1
        ?.?????##...??? 1,6,2
        ???###???#????? 6,1,1,1
        ??#????????.?.??.? 4,1,1,1,1,1
        ?.???###???##??# 1,2,3,2,1
        ?..?????#??.???.?#? 1,6,1,1,1
        ??.?.??#???? 1,5
        .?.#????#??????#?# 1,7,1,1,1,1
        .???????#??#?? 1,7
        ???.?????#.? 1,6
        ?#??#?.??????? 1,2,1,4
        ????#?#?.#????????? 3,3,1,6
        ??##?.???? 2,2
        ?#??.????#?? 2,1,5
        #?.??.?#?.??#?.??# 1,1,2,2,1,1
        ?#?#??.??#?????? 5,3,1
        #?#?#????.??...?? 1,4,1,1
        ????.?#?#?#. 2,4,1
        #?????#??????.?? 10,2,1
        .?.????#.?????? 1,4,1,2
        ???#?#?##????#??? 9,3
        #??.#?????.???? 2,1,1,1,2
        ??.????????#??? 2,1,1,1,3
        ?#?.#???#???#??#?##. 2,15
        ??#???#??????#? 8,3
        .??....???? 2,1,1
        ?.???..#??.???????. 2,2,5
        .???????#?. 3,1
        ????##?####??????#. 1,13,1
        ?..?###??.???. 3,2
        ?##?#?#?#?????.? 4,8
        ##??.#???????# 4,1,1,2
        .???.????## 1,5
        ????#????#????#???#? 2,7,6
        ??#????#?#.????????? 1,1,1,1,1,8
        ??.??.??##????????# 1,1,4,1,1,2
        ??.????.??##?# 1,4,5
        ?#?#?.?###??.?.. 4,6,1
        ??.##?.?#? 2,2
        ??#??.???..?? 3,2,1
        .#.?????.??. 1,1,1
        ???????#????.?##? 6,2,1,4
        .????#?????????###? 6,1,2,4
        .??#????.????.. 1,4,3
        #.#?????#?#????.??.? 1,2,6,2,1,1
        ?.???#.?#??????.?. 1,4,2,5
        ???.?##.#?##. 2,2,1,2
        ?.?####??#????#? 7,3
        ?##.?#???#?#.?.??#. 3,1,4,1,1
        ?????.????#???. 2,1,1
        ...?#?#?#?.#?#?? 6,5
        ?#?..??#?#? 2,5
        ?#?..??????????# 2,1,1,4
        #.????????.???#?#. 1,1,5,2,1,1
        #???????#?. 2,4
        ??????.###?.??????#? 1,1,3,5
        ??????.##?????#? 3,8
        ?#??.##???? 3,6
        ???.?????#?.?#??? 5,1
        ???#???#???.? 1,7,1
        ?#..?.?.?# 1,1,1
        ?.???#???#?? 4,4
        ???????###???.#. 13,1
        ??#???.#?#?#.??? 3,5,3
        ##..##?#???? 2,4,1
        ??..??.?#???? 1,2,2,1
        ????????#???#??#?# 2,1,3,8
        ??##?.?????#??#?..? 1,3,8
        ###??#??#?#???#??#? 3,6,3,2
        .??.??.??##??? 2,1,3,1
        ???#???#?#.#??#.? 1,5,1,1,1
        ???#??##.????. 1,6,1
        #.??##??#?#??.???? 1,1,9,1,1
        .??.?##??#?. 4,1
        ?.??#????????.#??? 3,5,2
        .??????#.? 1,3
        ????#?#???????#?#. 1,2,1,1,7
        ?.???#.#??#??#? 3,2,5
        ???.#?#??.??#???.?. 4,3
        ?.?????##?#?? 4,1
        ??#???#???##??????? 2,14
        ???????#?.. 3,2
        #???.?##?##???##... 1,10
        ??????#??.??.. 6,2
        ..#?.????###.#??# 2,5,4
        ???.??.#???#?.# 2,1,1,3,1
        ????.??#????#?.?? 2,2,1,3,1
        ??##..?.???? 4,1
        .???#?#???#.?.#?#?## 9,6
        ?....????####? 1,7
        ?#??#?#???? 1,4,3
        #??#???..# 1,4,1
        ???#????#??. 2,4,1,1
        ???????.?..? 1,1,1,1
        ?#??#?????????? 2,6,1,2
        ?.#???.????#????#?? 1,7
        ?????.#??#???#? 2,1,4,3
        .?###?????????. 8,4
        ?.#?????????##??#? 1,1,1,10
        ??#??????.. 1,3
        ??#??#...?#.?#?????. 5,1,7
        ?#?..??????.##.??? 3,6,2,1
        #.#.??.????#??????? 1,1,1,5,1,3
        #.??#?????#?#??.???? 1,12,2,1
        ?##?..##??#???? 2,2,3,1
        .?#???.????#?? 1,2,3
        #?.???????#???# 1,1,1,6,1
        ?.??.??#..??.#?## 2,2,1,1,2
        .???.????? 1,1,1
        ?#???????? 4,1,1
        ?????..????#??#? 1,6,1
        ??#????#????????? 8,2
        ?.???.?.???## 1,1,1,3
        ?..?.?..?. 1,1
        ?..???.?#?#?#?#? 1,8
        ???###???#.?.#.?#??? 10,1,1,4
        #.?#??#?.??.? 1,6,1,1
        #??????????#?????? 8,2,2
        ??##?.#??. 3,2
        .#?#??#??????#. 1,1,3,1,1
        ???.?..??.??#?? 2,1,1,5
        .????#????#????..?? 1,8,1,1
        ?##?##.??## 3,2,2
        ??????.??.# 4,1
        ??#.???#??.?#????#? 2,1,1,1,2,2
        .???#??###???#?# 4,3,5
        ??##?#?#?#?..#??? 9,3
        ?#?#???.?# 1,3,2
        ????#?#??.? 1,5,1
        ?.??????.? 1,3,1
        ?.?.??.?#?..??#??? 1,1,3,1,4
        .??##???#?##??. 3,1,3
        ??##?.??.?????. 4,1,1
        ???##??#??#??..???? 1,2,5,1,1
        ??????#????..? 1,2,5,1
        ??#?.?.??# 1,1,1
        .????????.?? 1,1,1,1
        .?#?.?##?#?? 3,5,1
        ???.???#?.. 2,2
        .???#??.??? 1,2,2
        ??#??????????? 1,5,1
        ??.?.?##..#? 1,1,3,1
        ??????#??# 2,1,2
        #?.???##??.?? 2,1,3,2
        ?..?#?.?????## 2,4
        #?##?.##??#?????#?? 5,8,3
        ????#?..???? 2,4
        ?.????#?#????? 2,5
        ???#?????.????? 6,1,1,1
        #???#?????##?????? 2,5,4,3
        #?#?#.?#??? 5,2,1
        .?#??.?????#?###. 2,1,2,5
        ??##???##????#??? 2,4,4
        ???.#??????.???? 1,1,7,1,1
        ??.???#???#?????? 1,2,1,1,7
        ##???#???#???. 2,1,2,5
        ??.#?...#?? 1,2
        ?#????#.?#???#?#?#? 2,1,1,11
        ?###???????# 6,3
        .??##?.??.????????# 4,1,3,4
        .?????#??? 1,3
        #????##?#??????? 2,2,6
        ?#?.?????????.# 2,1,5,1,1
        ??.?????#?? 1,1,3
        .?#####?.#??? 7,2
        ..???##?..??? 5,1
        ???##???#?? 2,4
        #.?#..???##????? 1,1,10
        ?????????????? 7,2
        ?.???.???# 1,2,3
        ??#??????#?.??? 4,2
        ?#??????..#?.?? 6,2,2
        ??.?#?..#.?????# 1,2,1,2
        ?.??#???.??#??? 4,2
        ?#??.????.?????. 2,1,1,5
        ..??.#?#.??#.???#? 1,1,1,1,1,3
        ?#??#??.?. 3,1,1
        ???????#??#??##?.? 11,2,1
        ?#??##.#??.????#???? 6,1,1,8
        .?##?#?##????? 11,1
        ??#?#???#????.?..??? 11,1,1,1
        ???????..##.?##? 4,2,4
        ??.#?#.??. 2,3,1
        ????##.#??? 2,3,3
        ?#..?.?#?#??.????# 2,6,5
        ????#?????. 5,1
        ???.?.????#. 3,5
        ?.??#?###? 1,6
        ?#?..???????? 2,6,1
        ????????.???#?? 1,3,1,1
        .?###???????#???? 8,5
        ????#???????? 8,2
        ..???#??.#.# 1,2,1,1
        #???#?#.?? 1,5
        ???#?.??#? 2,2,1
        ?#??#?????#?#?. 2,2,1,1,1
        ?#.?.#????.?#?????? 2,3,1,5,1
        .????#?.#?.?#?.?.?.? 5,1,1,1,1,1
        .?????###??##???.? 12,1
        ?.???#.?#??##.?? 1,1,2,3,1
        ?##?.????????##. 4,5,3
        #??????????. 1,1,2,2
        ??#?????##?#. 1,7
        #????.??#?##???? 1,2,7,1
        ???#??.??.## 6,1,2
        #???#..???? 5,1
        ?..#??##.?.#??. 5,3
        ?#?????#.#???? 2,4,3,1
        ??#??#????? 4,2,1
        ?.??##??.#??? 3,1
        ???.#??#???????#?.? 3,4,4,1
        ?#?#?#????.?.?#???? 9,3
        ??#??##????#??????? 12,1
        ??????.???#?? 3,4
        ??#???#???##..#?. 11,1
        #?????.?.. 1,1,1
        .?????##?. 2,2
        ???..??#?.? 1,3,1
        #??#???##?? 2,7
        ???????#??.?? 1,3,2
        #..?#???.#?.??? 1,2,2,2,2
        ?#???#.??.?.? 6,1,1,1
        ??#?...??#??? 2,5
        ?????#.##?#????? 4,1,2,5
        ???.?.?.???.????? 2,1,2
        #.?..??????.???? 1,1,5,1,1
        ??.???.???#??? 1,2,2,1
        ?#??????????. 1,1,2,1
        .???##?.?.#.?##???. 2,2,1,4
        ??.#????###??.#?.. 1,10,2
        .#?#?.?????? 4,2
        ?.?#?????? 2,3
        ..?#??????????..#?? 11,1
        .???#???##???????##? 1,3,3,1,3
        ????#????? 1,1,1
        ?#???????##??? 11,1
        ???????#????#. 5,3
        ?##?##???..#??.??? 5,1,1,1,3
        ??..?.?##?#??????# 1,12
        .????????????#???? 4,1,1,3,2
        .????#???????.? 1,7
        ?????#????#?????? 6,3,1
        ?.?#??...?.???##??.. 3,3
        #??????#?????.? 4,1,2,1
        ??#..??#??????? 1,1,3,1,1
        #??#?.??.?.??..#? 5,1,2,1
        ?#?#????????????#.?? 4,7,3
        ?#???.??#????#? 1,1,3,1,1
        ??#?#???#?#.#?.#? 1,2,4,1,1
        ???#???.#? 4,1
        ?.?#.??##.???# 1,3,1,1
        #???????.?.????? 1,1,1,2,1
        ?#??##?..???#???#??? 1,2,11
        ?????.?????#???????? 1,1,5,3
        #?.???#???? 1,5
        ???##???#.#. 9,1
        #.???????#? 1,5,2
        ##?.??##?.?? 3,3,2
        ???????????#????? 12,1
        ???.###??????##?? 2,6,3
        ??#.????.?? 1,4
        ?.??????#?????# 1,2,5,1
        ?????#???.?? 4,1
        ???????#?#????.? 1,1,1,4,1
        ?#.#.???????#???.??? 1,1,1,8,2
        ???.?##??? 1,4,1
        ???#????.?#.#?????# 1,6,1,1,1,2
        ???.???#????#?????.? 2,1,7,3
        ??##?????#? 5,1,1
        .?.##????#?. 1,2,2
        ????.?#?.## 1,2
        ??????.#??##??##?.# 1,3,1,2,4,1
        ?#??#?????????.?#??? 8,2,1,2,2
        ???#?????#?. 2,3
        ???#??????# 6,1,1
        ?#.#.?.??.?? 1,1,1,1
        ???#??.?.???#???##? 3,9
        ?#????###.?#? 1,1,3,2
        .?#?.???##. 1,2,2
        ????#?.?#.???? 3,2,2
        ???#????#???#???? 3,2,2,3,1
        ??#?????????##???? 1,1,9
        #.?..#..?#?.? 1,1,1,3
        ???#??.??#?#???. 4,6
        ?.?.???#???##??#. 1,5,4,1
        #????#.??#??#?#?? 6,5,2
        ??#.??#???#???#???? 1,10,1
        ?#?#.##?.#?? 4,2,2
        ????#??????##???? 1,1,5,2
        ..##??##????? 2,5,1
        ..?????????? 1,6
        ?##?#?.????. 4,1
        ???????#.???? 3,1,3
        #.#??????#????#. 1,2,1,1,3
        ???#???.?????? 5,2
        ..???#?#?.???? 1,1,1,4
        ??.#??.?#? 1,1,1
        ???#?.??.??????#???. 2,7
        ???##????.??? 1,3,1,1
        ?#..?#??.?? 1,2,1
        #.?#????.? 1,6
        ?..?#??.###????# 1,3,3,2,1
        .#..??????#???.?? 1,9,1
        ???????.?#..??????? 5,1,2,2,1
        ??#.#??##.. 1,1,5
        #???????#????#?.?? 1,13,1
        ?????.??#? 1,1,3
        #.?#???.?.#.#?#??? 1,4,1,1,1,1
        ???##???.??#.?? 7,1
        .??#??#??.?.???? 8,3
        ??#??????.????#? 7,1,1
        #??.?#.???.?????? 2,2,1,1,3
        .??.?.?.?#??? 1,5
        ????.??#??##. 2,1,3,2
        ???.??.?.???#???#.?? 1,1,1,1,8,1
        ??##..???. 1,2,3
        ..???#????? 1,4
        ??.???????? 1,3,1
        ??#.?##.???? 2,2,1
        .??#?##??.??#? 6,2
        .#??.#????#??#????? 1,9,2
        ?#??#??????? 3,1,3,1
        ??#?#??#????#.???. 10,1,1
        ?????#?????##?? 1,3,7
        #???????.#???#???#?? 1,1,1,1,6,4
        .???###??## 1,3,3
        #?#?#?#??.?#####??. 1,1,3,8
        .???????????##? 1,5,3
        ..?.?#?#?? 1,6
        #????#?..? 3,1,1
        ????#??#?.??#???? 5,4
        ????#.#?##??.??????? 4,5,1,2
        #???#?#??#??#..??#. 13,1
        .#???.##.?.??#??#?? 4,2,1,1,5
        ??..?.????? 1,1,2
        ??????????.?????#?? 10,3
        .??#??###??#??#?? 10,1,1
        .####.???.? 4,1,1
        ?..?.###??.? 1,3,1
        ???????..????????? 3,9
        ?.??????????. 1,6
        .??.???#????? 2,1,3,1
        ????#????#? 2,1,1
        ???..??????? 1,3,1
        ..???????.##?. 5,2
        ?#??###??# 1,3,1
        ???#..#??????? 1,1,3
        .???#??.?#.???????# 3,1,1,2,1,1
        ?...#.??.?#?? 1,3
        ???#?#???.#??. 1,3,1,2
        .????????????.??.. 7,1
        ?.????????.#?.??#. 1,4,1,1,3
        ??#?###.?..##?#?.? 5,5
        ?#.?.?#?.?#?????. 2,2,6
        ??##?#???.?#?#?????? 6,1,1,1,2
        ??????.??? 1,1,1
        .?#???##??#??? 6,2
        ??#????.??#???. 6,3
        ?????????#??.? 2,5
        ??##?????????? 2,1,1,1
        ?????.?#??.# 3,2,1
        ??#?#????.?? 5,1
        ????.?#?#?#????#?.? 4,1,1,6,1
        .?????.???#. 1,1,2
        ##????????#.????? 2,3,1,1,1
        #.??#???####.? 1,10
        ??#???#.??????? 4,1,2,1,1
        ?#?#??#.?#.?? 6,1,2
        ??##??#????.??? 7,1
        ?#?????#?#??#??? 2,7,1
        ?????.????. 2,1
        ?#???#??#.. 2,3,1
        .??##??????.. 5,2
        .??###??#??? 5,1
        ??????####??##???? 3,11
        ?.#?#?#?#??? 1,5
        ?#??#..??????#### 5,1,1,1,4
        .??#..##?#??.#.#?? 1,1,4,1,1,3
        ?????##??##?##?? 7,5
        .??.?#???...???#???. 1,5,4
        ?#?#.??????.. 3,4
        ??????.??. 2,2
        #..?????????#???? 1,1,1,4,1
        ???????#???????# 9,2
        .#?.???#?#?? 2,1,2
        ?##?#????##??? 3,1,1,5
        #??????#?.?#?.?? 1,2,4,3,1
        .?????##??#????# 1,7,1
        ?????#?#??? 7,2
        #??????##?.#???. 5,2,2
        ????##???#??##?.??? 14,2
        ???.?.?????? 1,1,1,1
        ?..?#.???????????? 1,2,9,2
        ?????????.?#???.# 8,3,1,1
        .???#??.????.##??? 1,3,1,1,3
        ??.?#??#????##.? 5,2
        ??????.?????#??? 5,1,5
        ?.?????..??#??..?. 5,3
        ?.????????? 1,6,1
        ???#?.?????. 1,2,5
        .##.#?##?#??????? 2,8,1
        ??#?.????? 4,1,1
        ?#????##.#.#??? 3,3,1,1,1
        ??.?#.?????.. 2,4
        ???#.??##. 2,2
        ?????????? 5,1
        #????##?.? 1,4,1
        ?#?###???.?.?. 1,6,1
        ???####?#.?..?.#. 8,1,1
        ??????????.?????#? 1,1,3,1,1,1
        ????#?#?.?#??..## 7,1,1,2
        ???.???#???# 1,5
        ??.??????#?##??? 1,7,2,1
        #?.#???##??##?.?.? 2,2,7,1
        ???????.????????. 1,1,2,7
        ??#.?.??##? 2,1,4
        ?#..????..? 2,1,1
        ????#?#####???#??? 12,2,1
        #?#?#?#?#???????# 10,1,3
        ??#.#?????.????????? 1,2,1,7
        ????.?.#???.??? 1,4,1
        ?????##??#?##... 1,8
        .##??????????? 3,2,1,1
        .#??.??#??.##?#? 3,3,5
        ???#????.????? 7,2
        ?.?.??????.??. 1,1,1,2
        ..???????##????#??? 1,7,4
        ???????#?.?#?#??.# 2,3,1,5,1
        ???????#???# 4,2,2
        #????.??#?????#?. 1,3,1,2,1
        ??.??.#??##. 1,5
        #??#?..##?#? 1,1,5
        ???...????#????##??? 2,2,4,5
        #??#????.?.?????.#? 7,1,2,1,1
        ??.?.?#??##???#?##?? 1,1,2,12
        ?#?..??????#??#???? 2,6,4
        ??.?????????#?.? 1,6,1,2
        ?????#???#?#?##.??#. 12,2
        .####???#.???# 5,2,1,1
        ???.????#? 1,2,2
        ??##???#.? 4,2
        ???..?#??#? 2,5
        ##??#??#??#???? 2,5,2,1
        #?????.??#? 6,3
        ??.????#???##? 2,6,2
        ?????????##????##??? 1,3,11
        ??????.???. 3,2
        .?.#.?????????????? 1,1,1,1,1,4
        .????.???#????#????. 3,8
        .??????????.#.#??? 9,1,4
        ?#..?.?##???##?.# 1,1,9,1
        ???#??#????.?# 3,1,2,1
        ??.?.????? 1,1,2
        ?.???#?..?? 1,2,2
        .?##?#???.?? 5,1,1
        ??..?#?.???#??## 2,8
        .#??.#???.#?#???..? 2,2,1,3,1,1
        ???.??##???#???#? 2,2,7
        ??????#???##????.?# 2,1,4,2,1
        ?????..????#?? 5,1,2,1
        ??###??#??.??? 5,4,1
        ???##???.# 5,1,1
        .???.???#?.?? 1,4
        ????#????.#?.??#? 3,2,2,3
        ?..???#?#???? 1,1,3,2
        ?##?#?.?..? 3,2,1
        ?..#?..?.?.##??? 1,1,1,5
        #??#?.#?...#? 1,2,1,2
        ??...??..??.. 1,2,1
        ?#?????#????#?#????? 1,1,1,1,1,7
        ?.??#????#?# 1,1,1,5
        ????????.?. 2,2,1
        ?.???#?????#.#??? 1,10,1,1
        ????..?.?.#????????? 3,1,1,4,4
        ?#???.?.?#????.#???. 5,1,2,1,1,4
        ??#???..?#??????? 1,1,5
        ?#??#?????????? 1,3,7
        ?#????...##?#?# 5,4,1
        .??.?..??##?????? 1,1,4,2,1
        #????#??.???#.##.? 8,1,1,2,1
        ???#??.???####??? 1,3,7,1
        .????#?#.?#### 2,4,5
        ?.#??.???#???#??##. 2,10
        ???????????##. 9,2
        .??#?#?????#?? 5,5
        .?#???#??#?? 3,4
        ???..?????..??? 1,1,1,2,3
        .##???#??????#?##? 7,5
        ?#??####?#??????? 9,3
        ??#?.??.?.####? 1,1,1,4
        #?##?#??.#?#???# 1,6,1,1,2
        .#?????#??#??? 2,1,6,1
        ?.#.???#??? 1,1,1
        ??#??#??#?.?? 3,1,1,1
        #???.#???#??##??#. 4,2,1,4,1
        ?.??#?#??? 5,1
        ???.?####?.???.????? 6,3
        ???.##??#?? 1,2,1
        .???#??.#???##???.? 3,8
        .######???????? 6,3,1,1
        ?.?#?.???..? 3,2,1
        ??????#????#??????.# 14,1,1
        ??.?#?##????.#.??#?? 1,1,2,2,1,4
        #?#?????#?#??#?.??. 1,2,8,1
        ?#??.#???#??.##?.? 4,1,4,2,1
        ?#??#????#??###??? 6,8
        ??????#?#? 1,7
        ????#?.?.???? 5,1,1,2
        ???..?#?#?? 1,4
        ??#?##?...?. 1,3
        #??.?#????### 1,1,4
        .#?##???.?#?? 4,1,1
        ??.#.?????#???#??. 2,1,1,5,1,1
        ?????.???. 1,1,3
        .?.???#???#.???#. 7,3
        ???????#??????.???? 1,1,3,4,1
        #???...???###????##? 1,2,7,4
        ?#???#???#??#?#?..# 6,1,1,1,1,1
        ?????##?#?#??.#??? 10,2,1
        ?????####?#??#??. 11,1,1
        ?.???##??????.? 1,7,2,1
        #?.?????#??.??????.. 1,4,3
        ??#???#??????#??.? 2,10
        ??#??.????.. 2,2
        ????#??..?? 1,4,1
        ????????#?? 1,7
        ??#?.??????.?.???? 1,1,4,1,3
        ?.??#..??. 1,1,2
        ?#.???????#?????.?.? 1,9
        ?.?????????#.???? 10,1,1
        ?#???#?????????????. 7,5
        ?????#?.???.??#?. 2,1,1,2,4
        ..?...??#.?#?#.###? 1,3,1,1,4
        ??#.?##.??????. 2,3,1,1,1
        ????.?#??#???# 1,1,9
        ??#???###?#????# 8,6
        ?##?#?#???#????. 3,5,4
        ?#?#?????#??????#.?# 4,5,1,2,1,1
        ..??#????#?? 1,1,1,1
        ????#?#?#.#?? 2,1,3,3
        ?????##???#???#. 6,6
        ?.#...???##.????? 1,2
        ?????....?# 1,1
        ????????###??????#? 1,2,8,1,1
        ?###?????#..????? 9,3
        ????.??????.?. 1,2,2,1
        .??????#?????.?#?? 1,4,3,2
        ..???????. 1,2
        ??.??????? 1,1,1
        ????????.?.??? 2,2,1,1
        ?????????.? 6,1,1
        ??#????##?#??#??#? 3,1,10
        ?.??..#??????#?.? 1,2,8,1
        ?.????..#?.. 2,2
        ??.??????#??#????? 1,1,1,1,7,1
        ????????#?.#?#?.# 8,1,1,1
        #.#?.#.??##?????? 1,2,1,7,1
        ???????.???. 1,2,1,1
        .#???###???.?#. 3,5,1
        ?????????#?? 1,1,5,1
        ???????##???? 1,7
        ??????.??#?# 3,1,2,1
        #???.?#???#? 3,4,1
        ?????????#? 4,1,1
        ...##?###?.?..?? 7,1
        ??.?#??#?????????? 1,2,1,5,2
        ????#???#?????? 2,1,6,1
        ??#?.???????? 4,1,1,1
        ###?????????#?.#?#?? 5,5,1,1,1,1
        ??.??##?.?. 1,4
        ????.????#???????? 1,3
        .?#??##??.??????#??. 8,8
        ?..??#??#?.??. 5,1
        .#?#?????# 4,1,1
        ?#??#..?#????? 1,1,4,2
        #??#.??????# 2,1,6
        ??????..????.. 4,3
        .##????????#??#? 2,1,1,6
        .???.???##? 2,4
        ?.??#?????#??. 1,5
        ???#?.?##?? 2,3
        ?#...?.#..?#?#?#?#?? 1,1,6,3
        ???????###?? 2,5
        #?#.??#.#?.?#?? 1,1,2,1,3
        #???.?.?##. 3,1,3
        #?.??###??. 1,6
        ..?????????.??# 4,1,1,1
        ??.?????#??? 1,1,2
        ??.?#???????? 1,5,1
        ??.?.????????#?##? 1,1,2,8
        ?###??.??. 6,1
        .?.#???#.? 1,1,1
        ????????#?. 3,3
        #.###??#?#????? 1,8,1,1
        ???????.#?#????.? 1,5
        ??#????..??#??#?.# 1,1,1,1,4,1
        #??#??????????? 1,5,1,1,2
        ??##.?#???#??? 2,5
        #.????#.??.? 1,1,1,1
        ????###????. 6,1
        .#?###?#.?#??? 7,1,1
        ??????.??.#????. 3,1
        ?#???##??#.#?..? 9,1
        ???????#?#? 3,4
        ?#?????##?.?#?#???.? 2,6,7
        ???#?.###??#?. 5,6
        ???#?.##??#?#? 1,3,1,2
        ???????#???#??????. 2,8,1,1,1
        .#?.??#..? 1,3
        ????????..??? 2,3
        ????#????.?# 5,1,2
        ?##????.?.?. 5,1
        ###?#?????#???#??.# 5,2,7,1
        ??#?#..???? 3,3
        .??.??????.#?? 2,2,2
        .??.?#.??#???????? 2,1,1,1,1,1
        ???#??##???..?? 1,9,2
        ...#???.##?.?? 1,2
        #??.??.?..?#?? 3,1,1,1
        ???#????.????#??. 2,2,2,3
        ?.???.???..???#.? 1,1,4
        ?.??#?#?#?.#??#??#? 8,7
        ?#..????#? 1,2,2
        ?#.#.#????????###? 1,1,1,10
        .????????? 5,2
        ????#?#.??.??# 7,1,1,1
        ?????##??????.???.# 1,1,6,1,3,1
        ???.????#.? 1,5
        ??#????#?#? 6,2
        .#.?##?#?? 1,2,1
        .?????#??#???.#? 3,3,4,1
        .#.????#?.??? 1,6,2
        ????#??#??#?#? 1,2,7
        ?#????.#?#??#??#? 2,2,3,6
        ??#??#?.??????. 7,2
        ##?.???.##???##?# 2,3,7,1
        ??????##?#.????? 8,1
        ..#?#??????#??#?. 1,1,9
        ?#????.#.????????#?# 1,1,1,1,9
        ????#???#?.??##? 3,6,4
        .???.#?#.??##?#?? 2,3,6
        ??.?#??.#???.?.?#?#? 2,2,1,2,1,4
        ##???#???.?#??.?#??? 6,2,1,1
        ##?#???.??#????? 5,1,3,2
        ?#?????????#??.?.?? 1,3,5,1,1,2
        .????.?##.#?#.#. 1,3,1,1,1
        ?????###?#?????? 8,2,2
        ?.?#????#???#???. 1,2,1,1,5
        ???.??????.??.?#??#? 1,5,1,5
        ???#??#??#??? 6,3,1
        ???#??.??#.?.?.. 3,1,1,1,1
        ?#?#?????.. 5,1
        ???.???##???#?? 1,2,3,1
        #???????#? 3,1,3
        ?##?.?.?.???? 3,1,1,2
        ????????..??#??? 1,1,6
        ??????#?.#.? 4,1,1
        .?#?##?##?#.???# 10,2,1
        ???#?#??.? 1,3
        ??##????????.?? 1,7,1,1
        ?#??.###?? 2,3
        ...?#?????.?? 5,1
        ????.??.??#? 2,2,2
        ???#?????????? 1,2,6,1
        ????#??.??.???## 5,2,4
        ##.??#???...#???#?? 2,1,2,7
        ?..??.?.#???? 1,1,2,1
        ?#?##????##?#? 1,3,4,2
        ????#?##???#?.? 3,8,1
        ?.??.##??.? 1,2,1
        ??#?????#??#?..?##?? 4,6,3
        ??##??..???.# 6,1,1,1
        ?????#???##?????.?# 6,7,1
        .??.???.??.??#???. 1,3,2,4
        ...???????#.???##?? 1,6,7
        ??.#.????. 1,4
        ????????#.?.?????? 8,1,3,1
        ??.???.##?##??#??? 1,8
        ???.???.?#???## 1,1,1,1,6
        .#????#????? 1,4,1,1
        ???#??#.?.#????.. 1,1,1,1,5
        ???#.?.#???#? 4,5
        ?#?#?##???.#. 1,1,4,1
        ??????.??..#??#??#? 5,1,8
        ??#?#?##??#??#? 1,1,1,5,1
        ??#??????#??? 1,1,4,1
        ?????.???##.??.?## 3,1,2,2,1,3
        ?###????.????#???. 4,8
        ??#?##?.##?#???##??? 1,1,2,6,4
        ???#????????.? 6,1,2
        #.?#.?????.??#? 1,2,4,1,1
        ???#???#??.???#?#??# 5,2,3,4
        ???#??.???? 1,2,1
        ???????.?..????. 3,3,1
        ????##?#.?.?.?????#? 6,6
        ???.##???.??#? 1,1,5,2
        ?????##?##??..????. 1,9,4
        ??#.#?#???.??? 1,1,3,1
        ???#??##????#?#. 1,7,4
        #?#?#?..??. 3,2,2
        .?#.??.#??????.#??? 2,1,2,1,2,1
        #??#????#? 5,2
        ?????.??.#??. 3,1
        ??.??.####?#?? 2,4,3
        ?#?.?????#?#???.??# 1,8,3
        ####???#??#?.#?? 11,1
        ????.####.? 1,4
        ????#??.#..??????#? 5,1,1,8
        ????????#??#.? 1,6,1,1
        ??#?#????#?.???? 2,7,1,1
        .????.?????? 1,1,4
        ??.????#?#?..?#.? 1,1,4,2,1
        .??????#????????? 3,1,2,2,1
        .?#?#?##??.#.???# 7,1,2,1
        #???#???#???.#?? 5,3,1,1,1
        """;
}